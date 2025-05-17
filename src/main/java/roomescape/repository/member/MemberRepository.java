package roomescape.repository.member;

import java.util.List;
import java.util.Optional;
import roomescape.domain.member.Member;

public interface MemberRepository {

    long add(Member member);

    Optional<Member> findById(long id);

    Optional<Member> findByUsername(String username);

    boolean existByUsername(String username);

    List<Member> findAll();
}
