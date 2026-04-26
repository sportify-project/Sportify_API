package sport.store.thinh.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sport.store.thinh.util.SecurityUtil;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    private String name;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String gender;
    private int age;
    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    @CreationTimestamp
    private Instant createdAt;
    private String createdBy;
    @UpdateTimestamp
    private Instant updatedAt;
    private String updatedBy;
    @ColumnDefault("true")
    private Boolean active;

    private String status;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;

    public Users() {
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
