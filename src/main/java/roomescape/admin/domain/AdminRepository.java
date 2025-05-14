package roomescape.admin.domain;

import java.util.Optional;

public interface AdminRepository  {

    Optional<Admin> findById(Long id);

    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);
}
