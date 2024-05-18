package roomescape.member.repository;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;

public interface MemberRepository extends CrudRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);

    default Member getById(long id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 회원 번호를 입력하였습니다."));
    }

    default Member getByEmail(Email email) {
        return findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] (email : " + email + ") 에 대한 사용자가 존재하지 않습니다."));
    }
}
