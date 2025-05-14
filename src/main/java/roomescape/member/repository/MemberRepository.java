package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberId;

@Repository
public interface MemberRepository extends JpaRepository<Member, MemberId> {

    boolean existsByEmail(MemberEmail email);

    Member save(Member member);

    Optional<Member> findById(MemberId id);

    Optional<Member> findByEmail(MemberEmail email);

    List<Member> findAll();
}
