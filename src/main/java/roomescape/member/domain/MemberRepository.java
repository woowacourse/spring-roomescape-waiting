package roomescape.member.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(Email email);

    boolean existsByEmail(Email email);

    boolean existsByName(Name name);
}
