package roomescape.repository.member;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.member.Member;

@org.springframework.stereotype.Repository
public interface MemberRepository extends Repository<Member, Long> {

    Member save(Member member);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Member> findAll();

    Optional<Member> findById(long id);
}
