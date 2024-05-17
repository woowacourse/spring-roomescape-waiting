package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.exceptions.NotFoundException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;

public interface MemberJpaRepository extends ListCrudRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);

    default Member getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다. memberId = " + id));
    }
}
