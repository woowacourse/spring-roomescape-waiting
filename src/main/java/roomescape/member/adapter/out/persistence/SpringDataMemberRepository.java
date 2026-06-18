package roomescape.member.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

interface SpringDataMemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByName(String name);
}
