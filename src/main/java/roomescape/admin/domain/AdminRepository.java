package roomescape.admin.domain;

public interface AdminRepository {

    Admin findById(Long id);

    Admin findByEmail(String email);

    boolean isExistsByEmail(String email);
}
