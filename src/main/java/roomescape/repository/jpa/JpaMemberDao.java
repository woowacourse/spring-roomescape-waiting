package roomescape.repository.jpa;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;

public interface JpaMemberDao extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAndEncryptedPassword(String email, String encryptedPassword);
}
