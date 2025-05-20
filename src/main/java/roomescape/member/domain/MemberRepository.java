package roomescape.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);

    void deleteById(Long id);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    List<Member> findAll();
}
