package sport.store.thinh.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sport.store.thinh.domain.*;
import sport.store.thinh.domain.dto.request.ReqOrderDTO;
import sport.store.thinh.domain.dto.response.ResOrderDTO;
import sport.store.thinh.repository.*;
import sport.store.thinh.util.SecurityUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        AddressRepository addressRepository,
                        OrderItemRepository orderItemRepository,
                        ProductVariantRepository productVariantRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResOrderDTO processOrder(ReqOrderDTO reqOrderDTO) {

        // 1. Lấy user từ JWT token (không tin vào client)
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Unauthorized: no authenticated user"));
        Users currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new RuntimeException("User not found: " + email);
        }

        // 2. Validate và lấy địa chỉ
        Address address = addressRepository.findById(reqOrderDTO.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found: " + reqOrderDTO.getAddressId()));

        // Kiểm tra địa chỉ có thuộc về user hiện tại không
        if (address.getUser() == null || address.getUser().getUserId() != currentUser.getUserId()) {
            throw new RuntimeException("Address does not belong to current user");
        }

        // 3. Tạo Order với đầy đủ thông tin
        Order order = new Order();
        order.setUser(currentUser);
        order.setShippingAddress(address);
        order.setNotes(reqOrderDTO.getNotes());
        order.setOrderStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        order.setShippingFee(BigDecimal.ZERO);   // Tính phí ship theo logic thực tế nếu cần
        order.setDiscountAmount(BigDecimal.ZERO);

        // Save order trước để có ID cho foreign key của OrderItem
        orderRepository.save(order);

        // 4. Xử lý từng OrderItem
        List<OrderItem> orderItemList = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ReqOrderDTO.ReqOrderItemDTO itemDTO : reqOrderDTO.getOrderitems()) {

            // Load variant từ DB — không tin giá từ client
            ProductVariant variant = productVariantRepository.findById(itemDTO.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Product variant not found: " + itemDTO.getVariantId()));

            // Kiểm tra tồn kho
            if (variant.getStockQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for [" + variant.getSku() + "]. " +
                        "Available: " + variant.getStockQuantity() +
                        ", Requested: " + itemDTO.getQuantity()
                );
            }

            // Trừ tồn kho và lưu
            variant.setStockQuantity(variant.getStockQuantity() - itemDTO.getQuantity());
            productVariantRepository.save(variant);

            // Lấy giá từ DB
            BigDecimal unitPrice = variant.getPrice();
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

            // Tạo OrderItem (chỉ 1 lần)
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(unitPrice);
            orderItemList.add(orderItem);
        }

        // 5. Lưu tất cả OrderItem trong 1 batch
        orderItemRepository.saveAll(orderItemList);

        // 6. Cập nhật tổng tiền cho Order
        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.add(order.getShippingFee()).subtract(order.getDiscountAmount()));
        order.setOrderItems(orderItemList);
        orderRepository.save(order);

        return convertFromOrderEntity(order);
    }

    public ResultPaginationDTO<ResOrderDTO> findAll(Specification<Order> spec, Pageable pageable) {
        Page<Order> pageOrder = (spec == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findAll(spec, pageable);
        
        Page<ResOrderDTO> page = pageOrder.map(this::convertFromOrderEntity);
        
        ResultPaginationDTO<ResOrderDTO> result = new ResultPaginationDTO<>();
        result.setResult(page.getContent());
        
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        result.setMeta(meta);
        
        return result;
    }

    public ResOrderDTO fetchById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return convertFromOrderEntity(order);
    }

    @Transactional
    public ResOrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        
        order.setOrderStatus(status);
        // Có thể thêm logic tự động đổi paymentStatus nếu status là DELIVERED...
        
        orderRepository.save(order);
        return convertFromOrderEntity(order);
    }


    // ─── Helper converters ──────────────────────────────────────────────────────

    public ResOrderDTO.ResOrderItemDTO convertFromItemEntity(OrderItem orderItem) {
        ResOrderDTO.ResOrderItemDTO dto = new ResOrderDTO.ResOrderItemDTO();
        dto.setOrderId(orderItem.getOrder().getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setVariantId(orderItem.getProductVariant().getId());
        dto.setVariantSize(orderItem.getProductVariant().getSize());
        dto.setVariantColor(orderItem.getProductVariant().getColor());
        dto.setProductName(orderItem.getProductVariant().getProduct().getName());

        // Tránh NoSuchElementException nếu product không có ảnh
        List<ProductImage> images = orderItem.getProductVariant().getProduct().getImages();
        if (images != null && !images.isEmpty()) {
            dto.setProductImageUrl(images.get(0).getImageUrl());
        }

        return dto;
    }

    public ResOrderDTO convertFromOrderEntity(Order order) {
        ResOrderDTO dto = new ResOrderDTO();
        dto.setOrderId(order.getId());
        dto.setOrderItems(order.getOrderItems().stream().map(this::convertFromItemEntity).toList());
        dto.setAddress(order.getShippingAddress().getAddressLine1());
        dto.setTotal(order.getTotalAmount());

        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getUserId());
        }

        return dto;
    }
}
