package roomescape.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    List<Member> findAllByRole(MemberRole role);
}
