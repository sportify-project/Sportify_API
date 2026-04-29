package sport.store.thinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import sport.store.thinh.domain.Supplier;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>,
        JpaSpecificationExecutor<Supplier> {

    boolean existsByTaxCode(String taxCode);
    java.util.Optional<sport.store.thinh.domain.Supplier> findFirstByNameContainingIgnoreCase(String name);
}
