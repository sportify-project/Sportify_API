package sport.store.thinh.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sport.store.thinh.util.SecurityUtil;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 20)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    public ProductVariant() {
    }

    @PrePersist
    public void prePersist() {
        createdBy = SecurityUtil.getCurrentUserLogin().get();
        createdAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedBy = SecurityUtil.getCurrentUserLogin().get();
        updatedAt = Instant.now();
    }
}
