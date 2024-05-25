package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Password;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmailAndEncryptedPassword(Email email, Password encryptedPassword);
}
