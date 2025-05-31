package roomescape.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    default Member getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 멤버입니다. id: " + id));
    }

    Optional<Member> findByEmailAndPassword(final String email, final String password);


    default Member getByEmailAndPassword(final String email, final String password) {
        return findByEmailAndPassword(email, password)
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 패스워드가 올바르지 않습니다."));
    }

    boolean existsByEmail(final String email);
}
