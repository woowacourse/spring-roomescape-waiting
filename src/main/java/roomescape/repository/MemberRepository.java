package roomescape.repository;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getById(Long id) {
        return findById(id).orElseThrow(() ->
                new NoSuchElementException("[ERROR] 존재하지 않는 사용자입니다.")
        );
    }

    Optional<Member> findByEmail(Email email);
}
