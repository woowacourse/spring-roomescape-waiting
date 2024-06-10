package roomescape.domain.member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    List<Member> findAll();

    Optional<Member> findById(long id);

    Optional<Member> findByEmailAndPassword(Email email, Password password);
}
