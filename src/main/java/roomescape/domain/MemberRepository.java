package roomescape.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(MemberEmail email);

    Optional<Member> findByEmail(MemberEmail email);

    boolean existsByEmailAndPassword(MemberEmail email, MemberPassword password);
}
