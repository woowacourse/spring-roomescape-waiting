package roomescape.domain.repository;

import org.springframework.data.repository.Repository;
import roomescape.domain.Member;

public interface MemberCommandRepository extends Repository<Member, Long> {

    Member save(Member member);
}
