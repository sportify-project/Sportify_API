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
import java.util.List;

@Entity
@Table(name = "goods_receipts")
@Getter
@Setter
@NoArgsConstructor
public class GoodsReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    /**
     * DRAFT      → Phiếu nháp, chưa duyệt
     * CONFIRMED  → Đã duyệt, tồn kho đã được cộng
     * CANCELLED  → Đã huỷ
     */
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReceiptItem> items;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @CreationTimestamp
    private Instant createdAt;
    private String createdBy;

    @UpdateTimestamp
    private Instant updatedAt;
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        createdBy = SecurityUtil.getCurrentUserLogin().orElse("SYSTEM");
    }

    @PreUpdate
    public void preUpdate() {
        updatedBy = SecurityUtil.getCurrentUserLogin().orElse("SYSTEM");
    }

    public void calculateTotalCost() {
        if (this.items == null || this.items.isEmpty()) {
            this.totalCost = java.math.BigDecimal.ZERO;
            return;
        }
        this.totalCost = this.items.stream()
                .map(item -> item.getSubtotal() != null ? item.getSubtotal() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
