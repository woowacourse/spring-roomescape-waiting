package roomescape.repository.member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;

public interface JpaMemberRepository extends JpaRepository<Member, Long> {

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    Optional<Member> findByUsername(String username);

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    boolean existsByUsername(String username);
}
