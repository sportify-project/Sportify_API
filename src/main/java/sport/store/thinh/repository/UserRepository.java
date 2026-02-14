package sport.store.thinh.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sport.store.thinh.domain.Users;

public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {
    Users findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Users> findAll(Specification<Users> spec, Pageable pageable);

    Users findByRefreshTokenAndEmail(String refreshToken, String email);
}
