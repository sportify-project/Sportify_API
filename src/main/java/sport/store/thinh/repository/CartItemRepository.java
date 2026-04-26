package sport.store.thinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import sport.store.thinh.domain.Cart;
import sport.store.thinh.domain.CartItem;
import sport.store.thinh.domain.ProductVariant;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);
}
