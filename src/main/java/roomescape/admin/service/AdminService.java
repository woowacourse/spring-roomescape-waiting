package roomescape.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.domain.Admin;
import roomescape.admin.domain.AdminRepository;
import roomescape.common.exception.BusinessException;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean isExistsByEmail(final String email) {
        return adminRepository.existsByEmail(email);
    }

    public Admin findByEmail(final String email) {
        return adminRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("관리자를 찾을 수 없습니다."));
    }

    public Admin findById(final Long id) {
        return adminRepository.findById(id)
            .orElseThrow(() -> new BusinessException("관리자를 찾을 수 없습니다."));
    }
}
