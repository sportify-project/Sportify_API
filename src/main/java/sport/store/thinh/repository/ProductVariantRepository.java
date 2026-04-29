package sport.store.thinh.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import sport.store.thinh.domain.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {
    Page<ProductVariant> findAll(Specification<ProductVariant> spec, Pageable pageable);
    java.util.Optional<ProductVariant> findFirstBySku(String sku);
    java.util.Optional<ProductVariant> findFirstByProduct_NameContainingIgnoreCase(String name);
}
