package sport.store.thinh.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    @ColumnDefault("true")
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Roles role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;

    public Users() {
    }

    @PrePersist
    public void prePersist() {
        createdBy = "createdBy";
        createdAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedBy = "updatedBy";
        updatedAt = Instant.now();
    }
}
