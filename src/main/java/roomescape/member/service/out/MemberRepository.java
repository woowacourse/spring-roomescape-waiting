package roomescape.member.service.out;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findByEmailAndPassword(String email, String password);

    Optional<Member> findById(Long memberId);

    boolean existsByEmail(String email);

    List<Member> findAll();
}
