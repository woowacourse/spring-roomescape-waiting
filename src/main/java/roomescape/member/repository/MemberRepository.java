package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;

public interface MemberRepository {

    boolean existsByEmail(MemberEmail email);

    Member save(Member member);

    Optional<Member> findById(Long id);

    List<Member> findAll();
}
