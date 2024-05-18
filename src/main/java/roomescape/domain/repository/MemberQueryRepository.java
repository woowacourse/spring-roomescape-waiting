package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public interface MemberQueryRepository extends Repository<Member, Long> {

    Optional<Member> findById(Long id);

    List<Member> findAll();

    Optional<Member> findByEmail(Email email);

    default Member getById(Long id) {
        return findById(id).orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_MEMBER,
                String.format("존재하지 않는 회원입니다. 입력한 회원 id:%d", id)));
    }

    default Member getByEmail(Email email) {
        return findByEmail(email).orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_MEMBER,
                String.format("존재하지 않는 회원입니다. 입력한 회원 email:%s", email)));
    }
}
