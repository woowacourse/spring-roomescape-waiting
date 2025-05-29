package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

public interface MemberRepository {

    Member save(Member member);

    List<Member> findAll();

    Optional<Member> findById(Long id);

    Optional<Member> findByEmailAndPassword(Email email, Password password);

    boolean existsByEmail(Email email);
}
