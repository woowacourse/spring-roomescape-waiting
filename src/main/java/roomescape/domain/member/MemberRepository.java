package roomescape.domain.member;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getByIdentifier(long id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 회원이 존재하지 않습니다."));
    }

    default Member getByEmail(String email) {
        return findByEmail(new Email(email))
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 회원이 존재하지 않습니다."));
    }

    Optional<Member> findByEmail(Email email);

    //    boolean existsByEmail(String email);
    boolean existsByEmail(Email email);
}
