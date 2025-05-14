package roomescape.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import roomescape.entity.Member;
import roomescape.repository.MemberRepository;

public interface JpaMemberRepository extends MemberRepository, CrudRepository<Member, Long> {

    Member findByEmail(String email);

    boolean existsByEmail(String email);
}
