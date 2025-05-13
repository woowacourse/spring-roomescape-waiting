package roomescape.member.domain;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Long save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmailAndPassword(String email, String password);

    List<Member> findAll();

    boolean existByEmail(String email);

}
