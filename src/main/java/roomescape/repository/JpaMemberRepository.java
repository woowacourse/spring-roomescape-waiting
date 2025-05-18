package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Member;

import java.util.Optional;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
