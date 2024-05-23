package roomescape.domain.member;

import java.util.List;
import java.util.Optional;
import roomescape.exception.member.AuthenticationFailureException;

public interface MemberRepository {
    Member save(Member user);

    default Member getById(Long id) {
        return findById(id)
                .orElseThrow(AuthenticationFailureException::new);
    }

    Optional<Member> findById(Long id);

    Optional<Member> findByEmailAndPassword(String email, String password);

    List<Member> findAll();

    boolean existsByEmail(String email);

    void delete(Member member);
}
