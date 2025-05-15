package roomescape.member.repository;

import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.service.MemberRepository;

@Primary
public interface MemberJpaRepository extends JpaRepository<Member, Long>, MemberRepository {

    boolean existsByEmailAndPassword(Email email, Password password);

    Optional<Member> findByEmail(Email email);

    Optional<Member> findByName(Name name);
}
