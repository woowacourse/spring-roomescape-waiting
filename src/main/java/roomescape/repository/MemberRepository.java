package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.model.member.MemberEmail;
import roomescape.model.member.Member;
import roomescape.model.member.MemberPassword;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmailAndPassword(MemberEmail email, MemberPassword password);
}
