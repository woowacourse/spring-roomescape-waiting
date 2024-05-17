package roomescape.domain;

import org.springframework.data.repository.Repository;

public interface MemberCommandRepository extends Repository<Member, Long> {

    Member save(Member member);
}
