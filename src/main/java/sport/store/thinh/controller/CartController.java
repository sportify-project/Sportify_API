package sport.store.thinh.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sport.store.thinh.domain.dto.request.ReqAddToCartDTO;
import sport.store.thinh.domain.dto.response.ResCartDTO;
import sport.store.thinh.service.CartService;
import sport.store.thinh.util.annotation.APIMessage;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/carts")
    @APIMessage("Get cart by user")
    public ResponseEntity<ResCartDTO> getCart() {
        return ResponseEntity.ok(cartService.getCartByUser());
    }

    @PostMapping("/carts")
    @APIMessage("Add item to cart")
    public ResponseEntity<Void> addToCart(@Valid @RequestBody ReqAddToCartDTO reqDTO) {
        cartService.addToCart(reqDTO.getVariantId(), reqDTO.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/carts/items/{id}")
    @APIMessage("Remove item from cart")
    public ResponseEntity<Void> removeFromCart(@PathVariable("id") Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/carts/items/{id}")
    @APIMessage("Update cart item quantity")
    public ResponseEntity<Void> updateQuantity(@PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        cartService.updateQuantity(id, quantity);
        return ResponseEntity.ok().build();
    }
}
