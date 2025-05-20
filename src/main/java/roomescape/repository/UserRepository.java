package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;

public interface UserRepository extends JpaRepository<Member, Long> {

    Optional<Member> findOneByEmail(String email);

    boolean existsByEmail(String email);
}
