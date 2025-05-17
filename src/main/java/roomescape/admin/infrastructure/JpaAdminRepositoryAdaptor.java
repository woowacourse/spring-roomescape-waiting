package roomescape.admin.infrastructure;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.admin.domain.Admin;
import roomescape.admin.domain.AdminRepository;

@Repository
public class JpaAdminRepositoryAdaptor implements AdminRepository {

    private final JpaAdminRepository jpaAdminRepository;

    public JpaAdminRepositoryAdaptor(JpaAdminRepository jpaAdminRepository) {
        this.jpaAdminRepository = jpaAdminRepository;
    }

    @Override
    public Optional<Admin> findById(Long id) {
        return jpaAdminRepository.findById(id);
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        return jpaAdminRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaAdminRepository.existsByEmail(email);
    }
}
