package roomescape.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);

    List<Member> findAll();

    Optional<Member> findById(long id);

    Optional<Member> findByEmailAndPassword(String email, String password);
}
