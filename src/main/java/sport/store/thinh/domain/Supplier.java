package sport.store.thinh.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sport.store.thinh.util.SecurityUtil;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @ColumnDefault("true")
    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GoodsReceipt> goodsReceipts;

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
}
