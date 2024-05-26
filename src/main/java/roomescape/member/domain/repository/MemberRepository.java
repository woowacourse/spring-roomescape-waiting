package roomescape.member.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.NotFoundException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(Email email, Password password);

    Optional<Member> findByEmail(Email email);

    default Member getById(final Long memberId) {
        return findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_NOT_FOUND,
                        ErrorType.MEMBER_NOT_FOUND.getDescription()));
    }
}
