package roomescape.member.application.port.out;

import roomescape.member.domain.Member;

import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findByName(String name);
}
