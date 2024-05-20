package roomescape.domain.member;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface MemberRepository extends ListCrudRepository<Member, Long> {

    boolean existsByEmail(Email email);

    Optional<Member> findByEmail(Email email);

    default Member getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));
    }

    default Member getByEmail(Email email) {
        return findByEmail(email).orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));
    }
}
