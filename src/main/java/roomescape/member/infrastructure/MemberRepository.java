package roomescape.member.infrastructure;

import roomescape.member.Member;

import java.util.Optional;

public interface MemberRepository {
    Optional<Member> findByName(String name);
}
