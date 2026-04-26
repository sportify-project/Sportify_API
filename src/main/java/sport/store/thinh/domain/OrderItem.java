package sport.store.thinh.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sport.store.thinh.util.SecurityUtil;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Price at the time of purchase

    @CreationTimestamp
    private Instant createdAt;
    private String createdBy;

    @UpdateTimestamp
    private Instant updatedAt;
    private String updatedBy;

    @PrePersist
    private void prePersist() {
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("SYSTEM");
    }

    @PreUpdate
    private void preUpdate() {
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("SYSTEM");
    }
}
