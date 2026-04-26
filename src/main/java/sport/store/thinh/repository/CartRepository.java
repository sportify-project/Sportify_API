package sport.store.thinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import sport.store.thinh.domain.Cart;
import sport.store.thinh.domain.Users;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(Users user);
}
