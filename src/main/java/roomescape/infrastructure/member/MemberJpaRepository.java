package roomescape.infrastructure.member;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;

public interface MemberJpaRepository extends MemberRepository, ListCrudRepository<Member, Long> {

    @Override
    boolean existsByEmail(Email email);

    Optional<Member> findByEmail(Email email);

    @Override
    default Member getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));
    }

    @Override
    default Member getByEmail(Email email) {
        return findByEmail(email).orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));
    }
}
