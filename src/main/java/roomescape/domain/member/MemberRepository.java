package roomescape.domain.member;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);

    List<Member> findAll();

    default Member getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 회원이 존재하지 않습니다."));
    }

    Optional<Member> findById(Long id);

    default Member getByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일의 회원이 존재하지 않습니다."));
    }

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
