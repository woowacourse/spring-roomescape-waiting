package roomescape.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findByEmailAndPassword(String email, String password);

    Optional<Member> findById(long id);

    Member save(Member member);

    List<Member> findAll();
}
