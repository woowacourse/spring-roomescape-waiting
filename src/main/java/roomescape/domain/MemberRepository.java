package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    Member save(Member member);

    List<Member> findAll();
}