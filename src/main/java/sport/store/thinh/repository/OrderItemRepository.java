package sport.store.thinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sport.store.thinh.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
