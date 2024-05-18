package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

public interface MemberRepository extends Repository<Member, Long> {

    Optional<Member> findMemberByEmailAndPassword(Email email, Password password);

    Optional<Member> findMemberById(long id);

    List<Member> findAll();
}
