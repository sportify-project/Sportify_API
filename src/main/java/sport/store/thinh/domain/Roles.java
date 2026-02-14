package sport.store.thinh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long  id;
    private String role;
    private String description;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Users> users;

    @PrePersist
    private void prePersist() {
        createdAt = Instant.now();
        createdBy = "admin";
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
        updatedBy = "admin";
    }

    public Roles() {
    }
}
