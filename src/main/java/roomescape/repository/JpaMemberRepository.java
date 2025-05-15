package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Member;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    Member findByEmail(String email);

    boolean existsByEmail(String email);
}
