package roomescape.infra.repository;

import org.springframework.data.repository.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.exception.member.AuthenticationFailureException;

public interface MemberJpaRepository extends MemberRepository, Repository<Member, Long> {

    @Override
    default Member getById(Long id) {
        return findById(id)
                .orElseThrow(AuthenticationFailureException::new);
    }
}
