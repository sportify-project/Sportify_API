package sport.store.thinh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sport.store.thinh.util.SecurityUtil;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private Users user;
    @Column(name = "full_name", length = 100)
    private String fullName;
    @Column(length = 20)
    private String phone;
    @Column(name = "address_line1", nullable = false)
    private String addressLine1;
    @Column(name = "address_line2")
    private String addressLine2;
    @Column(length = 100)
    private String city;
    @Column(length = 100)
    private String ward;
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    @Column(name = "is_default")
    private boolean defaultAddress;
    @CreationTimestamp
    private Instant createdAt;
    private String createdBy;
    @UpdateTimestamp
    private Instant updatedAt;
    private String updatedBy;
    private Instant deletedAt;

    @PrePersist
    public void prePersist() {
        createdBy = String.valueOf(SecurityUtil.getCurrentUserLogin().get());
    }

    @PreUpdate
    public void preUpdate() {
        updatedBy = String.valueOf(SecurityUtil.getCurrentUserLogin().get());
    }
}
