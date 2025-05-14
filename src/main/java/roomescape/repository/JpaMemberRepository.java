package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.member.Member;

import java.util.Optional;

public interface JpaMemberRepository extends JpaRepository<Member, Long>  {

    Optional<Member> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
