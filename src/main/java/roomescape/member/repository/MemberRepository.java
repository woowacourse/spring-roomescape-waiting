package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;

public interface MemberRepository {

    Optional<Member> findById(Long id);

    List<Member> findAll();

    Member save(Member member);

    boolean existsByEmail(MemberEmail email);
}
