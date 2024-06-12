package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;

@Repository
public interface MemberJpaRepository extends MemberRepository, JpaRepository<Member, Long> {

}
