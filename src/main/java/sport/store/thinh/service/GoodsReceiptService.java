package sport.store.thinh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sport.store.thinh.domain.*;
import sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO;
import sport.store.thinh.domain.dto.response.ResGoodsReceiptDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.GoodsReceiptItemRepository;
import sport.store.thinh.repository.GoodsReceiptRepository;
import sport.store.thinh.repository.ProductVariantRepository;
import sport.store.thinh.repository.SupplierRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SupplierService supplierService;

    public GoodsReceiptService(GoodsReceiptRepository goodsReceiptRepository,
                                GoodsReceiptItemRepository goodsReceiptItemRepository,
                                SupplierRepository supplierRepository,
                                ProductVariantRepository productVariantRepository,
                                SupplierService supplierService) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptItemRepository = goodsReceiptItemRepository;
        this.supplierRepository = supplierRepository;
        this.productVariantRepository = productVariantRepository;
        this.supplierService = supplierService;
    }

    // ─── Create ──────────────────────────────────────────────────────────────

    @Transactional
    public ResGoodsReceiptDTO createReceipt(ReqGoodsReceiptDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + dto.getSupplierId()));

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setSupplier(supplier);
        receipt.setNotes(dto.getNotes());
        receipt.setStatus("DRAFT");

        // Save receipt first to get id (FK for items)
        goodsReceiptRepository.save(receipt);

        List<GoodsReceiptItem> items = buildItems(dto.getItems(), receipt);
        goodsReceiptItemRepository.saveAll(items);
        receipt.setItems(items);
        receipt.calculateTotalCost();
        goodsReceiptRepository.save(receipt);

        return toDto(receipt);
    }

    // ─── Update (chỉ khi DRAFT) ──────────────────────────────────────────────

    @Transactional
    public ResGoodsReceiptDTO updateReceipt(Long id, ReqGoodsReceiptDTO dto) {
        GoodsReceipt receipt = findReceiptById(id);
        validateDraft(receipt);

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + dto.getSupplierId()));

        receipt.setSupplier(supplier);
        receipt.setNotes(dto.getNotes());

        // Cập nhật danh sách items bằng cách clear và addAll (để tránh lỗi orphan removal)
        receipt.getItems().clear();
        List<GoodsReceiptItem> newItems = buildItems(dto.getItems(), receipt);
        receipt.getItems().addAll(newItems);
        
        receipt.calculateTotalCost();
        goodsReceiptRepository.save(receipt);

        return toDto(receipt);
    }

    // ─── Confirm ─────────────────────────────────────────────────────────────

    @Transactional
    public ResGoodsReceiptDTO confirmReceipt(Long id) {
        GoodsReceipt receipt = findReceiptById(id);
        validateDraft(receipt);

        for (GoodsReceiptItem item : receipt.getItems()) {
            ProductVariant variant = item.getProductVariant();
            // Cộng tồn kho
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            productVariantRepository.save(variant);
        }

        receipt.calculateTotalCost();
        receipt.setStatus("CONFIRMED");
        receipt.setConfirmedAt(Instant.now());
        goodsReceiptRepository.save(receipt);

        return toDto(receipt);
    }

    // ─── Cancel (DRAFT hoặc CONFIRMED với hoàn tồn) ──────────────────────────

    @Transactional
    public ResGoodsReceiptDTO cancelReceipt(Long id) {
        GoodsReceipt receipt = findReceiptById(id);
        
        if ("CANCELLED".equals(receipt.getStatus())) {
            throw new RuntimeException("Phiếu đã ở trạng thái HUỶ.");
        }

        // Nếu phiếu đã CONFIRMED, cần trừ lại tồn kho đã cộng trước đó
        if ("CONFIRMED".equals(receipt.getStatus())) {
            for (GoodsReceiptItem item : receipt.getItems()) {
                ProductVariant variant = item.getProductVariant();
                
                // Kiểm tra xem tồn kho hiện tại có đủ để trừ lại không
                if (variant.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException(
                        "Không thể huỷ phiếu! Tồn kho hiện tại của [" + variant.getSku() + 
                        "] không đủ để hoàn tác (Hiện có: " + variant.getStockQuantity() + 
                        ", Cần trừ: " + item.getQuantity() + ")"
                    );
                }
                
                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
                productVariantRepository.save(variant);
            }
        }

        receipt.setStatus("CANCELLED");
        goodsReceiptRepository.save(receipt);

        return toDto(receipt);
    }

    // ─── Read ────────────────────────────────────────────────────────────────

    public ResGoodsReceiptDTO findById(Long id) {
        return toDto(findReceiptById(id));
    }

    public ResultPaginationDTO<ResGoodsReceiptDTO> findAll(Specification<GoodsReceipt> spec, Pageable pageable) {
        Page<GoodsReceipt> pageReceipt = (spec == null)
                ? goodsReceiptRepository.findAll(pageable)
                : goodsReceiptRepository.findAll(spec, pageable);
        
        Page<ResGoodsReceiptDTO> page = pageReceipt.map(this::toDto);
        ResultPaginationDTO<ResGoodsReceiptDTO> result = new ResultPaginationDTO<>();
        result.setResult(page.getContent());
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        result.setMeta(meta);
        return result;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private GoodsReceipt findReceiptById(Long id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Goods receipt not found: " + id));
    }

    private void validateDraft(GoodsReceipt receipt) {
        if (!"DRAFT".equals(receipt.getStatus())) {
            throw new RuntimeException(
                    "Phiếu #" + receipt.getId() + " đang ở trạng thái [" + receipt.getStatus() + "], không thể thực hiện thao tác này."
            );
        }
    }

    private List<GoodsReceiptItem> buildItems(List<ReqGoodsReceiptDTO.ReqGoodsReceiptItemDTO> itemDTOs,
                                               GoodsReceipt receipt) {
        List<GoodsReceiptItem> items = new ArrayList<>();
        for (ReqGoodsReceiptDTO.ReqGoodsReceiptItemDTO itemDTO : itemDTOs) {
            ProductVariant variant = productVariantRepository.findById(itemDTO.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + itemDTO.getVariantId()));

            GoodsReceiptItem item = new GoodsReceiptItem();
            item.setGoodsReceipt(receipt);
            item.setProductVariant(variant);
            item.setQuantity(itemDTO.getQuantity());
            item.setCostPrice(itemDTO.getCostPrice());
            item.calculateSubtotal();
            items.add(item);
        }
        return items;
    }

    public ResGoodsReceiptDTO toDto(GoodsReceipt receipt) {
        ResGoodsReceiptDTO dto = new ResGoodsReceiptDTO();
        dto.setId(receipt.getId());
        dto.setStatus(receipt.getStatus());
        dto.setTotalCost(receipt.getTotalCost());
        dto.setNotes(receipt.getNotes());
        dto.setCreatedBy(receipt.getCreatedBy());
        dto.setCreatedAt(receipt.getCreatedAt());
        dto.setConfirmedAt(receipt.getConfirmedAt());
        dto.setSupplier(supplierService.toDto(receipt.getSupplier()));

        if (receipt.getItems() != null) {
            dto.setItems(receipt.getItems().stream().map(this::toItemDto).toList());
        }

        return dto;
    }

    private ResGoodsReceiptDTO.ResGoodsReceiptItemDTO toItemDto(GoodsReceiptItem item) {
        ResGoodsReceiptDTO.ResGoodsReceiptItemDTO dto = new ResGoodsReceiptDTO.ResGoodsReceiptItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setCostPrice(item.getCostPrice());
        dto.setSubtotal(item.getSubtotal());

        ProductVariant variant = item.getProductVariant();
        dto.setVariantId(variant.getId());
        dto.setSku(variant.getSku());
        dto.setVariantSize(variant.getSize());
        dto.setVariantColor(variant.getColor());
        dto.setProductName(variant.getProduct().getName());

        return dto;
    }
}
