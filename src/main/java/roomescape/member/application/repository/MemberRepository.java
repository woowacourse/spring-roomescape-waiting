package roomescape.member.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@Repository
public interface MemberRepository extends CrudRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    List<Member> findAll();
}
