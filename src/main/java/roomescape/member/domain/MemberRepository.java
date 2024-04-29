package roomescape.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    List<Member> findAll();

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    boolean existByEmail(String email);

    boolean existByName(String name);

    Member save(Member member);
}
