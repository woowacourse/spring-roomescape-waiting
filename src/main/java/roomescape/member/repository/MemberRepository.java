package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;

public interface MemberRepository {

    Optional<String> findNameByEmail(final String email);

    boolean existsByEmailAndPassword(final String email, final String password);

    Optional<Member> findByEmail(final String email);

    Optional<Member> findById(final Long memberId);

    List<Member> findAll();
}
