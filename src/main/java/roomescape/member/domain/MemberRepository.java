package roomescape.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(Email email);

    boolean existsByEmail(Email email);

    List<Member> findAll();
}
