package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findByEmailAndPassword(String email, String password);

    Optional<Member> findById(Long memberId);

    List<Member> findAll();
}
