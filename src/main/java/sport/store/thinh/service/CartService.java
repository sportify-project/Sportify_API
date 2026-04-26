package sport.store.thinh.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sport.store.thinh.domain.Cart;
import sport.store.thinh.domain.CartItem;
import sport.store.thinh.domain.ProductVariant;
import sport.store.thinh.domain.Users;
import sport.store.thinh.repository.CartItemRepository;
import sport.store.thinh.repository.CartRepository;
import sport.store.thinh.repository.ProductVariantRepository;
import sport.store.thinh.repository.UserRepository;
import sport.store.thinh.util.SecurityUtil;

import sport.store.thinh.domain.dto.response.ResCartDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public ResCartDTO getCartByUser() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found in session"));

        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        List<ResCartDTO.CartItemDTO> itemDTOs = cart.getCartItems().stream().map(item -> {
            ProductVariant variant = item.getProductVariant();
            BigDecimal subTotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            return ResCartDTO.CartItemDTO.builder()
                    .id(item.getId())
                    .productVariantId(variant.getId())
                    .productId(variant.getProduct().getId())
                    .productName(variant.getProduct().getName())
                    .productImage(variant.getProduct().getImages().isEmpty() ? "" : variant.getProduct().getImages().get(0).getImageUrl())
                    .size(variant.getSize())
                    .color(variant.getColor())
                    .price(variant.getPrice())
                    .quantity(item.getQuantity())
                    .subTotal(subTotal)
                    .build();
        }).collect(Collectors.toList());

        BigDecimal totalPrice = itemDTOs.stream()
                .map(ResCartDTO.CartItemDTO::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResCartDTO.builder()
                .id(cart.getId())
                .userId(user.getUserId())
                .cartItems(itemDTOs)
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public void addToCart(Long variantId, int quantity) {
        // 1. Get current user
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found in session"));
        
        Users user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // 2. Get or Create Cart for the user
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // 3. Get Product Variant
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found with id: " + variantId));

        // 4. Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductVariant(cart, variant);

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Create new CartItem
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setUser(user);
            newItem.setProductVariant(variant);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void removeFromCart(Long itemId) {
        cartItemRepository.deleteById(itemId);
    }

    @Transactional
    public void updateQuantity(Long itemId, int quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }
}
