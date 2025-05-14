package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.Member;

public interface JpaMemberRepository extends JpaRepository<Member, Long>  {

}
