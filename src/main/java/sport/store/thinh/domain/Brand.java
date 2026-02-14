package sport.store.thinh.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sport.store.thinh.util.SecurityUtil;

import java.time.Instant;
import java.util.List;

@Table(name = "brands")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(length = 50)
    private String country;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "brand")
    private List<Product> products;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    private void prePersist() {
        createdBy = SecurityUtil.getCurrentUserLogin().get();
    }

    @PreUpdate
    private void preUpdate() {
        SecurityUtil.getCurrentUserLogin().get();
    }

    public Brand(String name, String slug, String description, String logoUrl, String country, List<Product> products, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.logoUrl = logoUrl;
        this.country = country;
        this.products = products;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
