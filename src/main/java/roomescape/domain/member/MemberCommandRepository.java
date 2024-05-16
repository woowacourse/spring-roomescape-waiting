package roomescape.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCommandRepository extends JpaRepository<Member, Long> {
}
