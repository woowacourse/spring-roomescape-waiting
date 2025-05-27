package roomescape.member.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.member.domain.Member;

public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    List<Member> findAll();

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);
}
