package roomescape.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Member;

public interface  MemberJpaRepository extends JpaRepository<Member, Long>, MemberRepository {
    Optional<Member> findByEmailAndPassword(String email, String password);
}
