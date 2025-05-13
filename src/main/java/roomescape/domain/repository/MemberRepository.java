package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;

public interface MemberRepository {

    List<Member> findAll();

    Long save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findBy(String email, String password);
}
