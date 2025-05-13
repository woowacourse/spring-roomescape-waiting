package roomescape.admin.service;

import org.springframework.stereotype.Service;
import roomescape.admin.domain.Admin;
import roomescape.admin.domain.AdminRepository;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean isExistsByEmail(final String email) {
        return adminRepository.isExistsByEmail(email);
    }

    public Admin findByEmail(final String email) {
        return adminRepository.findByEmail(email);
    }

    public Admin findById(final Long id) {
        return adminRepository.findById(id);
    }
}
