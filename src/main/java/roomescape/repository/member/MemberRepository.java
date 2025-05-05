package roomescape.repository.member;

import java.util.Optional;
import roomescape.domain.member.Member;

public interface MemberRepository {

    long add(Member member);

    boolean existsByEmailAndPassword(Member member);

    Optional<Member> findById(long id);
}
