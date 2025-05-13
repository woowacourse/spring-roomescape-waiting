package roomescape.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;

@Repository
public interface MemberRepositoryImpl extends JpaRepository<Member, Long>, MemberRepository {

}
