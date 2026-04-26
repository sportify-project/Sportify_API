package sport.store.thinh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sport.store.thinh.domain.Roles;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {
    boolean existsByRole(String role);
    Roles findByRole(String role);
}
