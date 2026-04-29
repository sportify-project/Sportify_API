package sport.store.thinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import sport.store.thinh.domain.GoodsReceipt;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long>,
        JpaSpecificationExecutor<GoodsReceipt> {
}
