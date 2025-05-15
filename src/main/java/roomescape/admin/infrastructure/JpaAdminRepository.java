package roomescape.admin.infrastructure;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import roomescape.admin.domain.Admin;

public interface JpaAdminRepository extends CrudRepository<Admin, Long> {

    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);
}
