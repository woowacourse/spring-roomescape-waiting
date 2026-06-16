package roomescape.member.application.port.out;

import java.util.Optional;
import roomescape.member.domain.Member;

public interface MemberRepository {
    Optional<Member> findByName(String name);

    Optional<Member> findById(long id);
}
