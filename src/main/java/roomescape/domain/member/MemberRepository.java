package roomescape.domain.member;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import roomescape.domain.exception.DomainNotFoundException;

public interface MemberRepository extends CrudRepository<Member, Long> {

    List<Member> findAll();

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    default Member getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new DomainNotFoundException("해당 id의 회원이 존재하지 않습니다."));
    }

    default Member getByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new DomainNotFoundException("해당 이메일의 회원이 존재하지 않습니다."));
    }
}
