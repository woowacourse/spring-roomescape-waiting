package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

public interface MemberRepository {

    Member save(Member member);

    boolean existsByEmailAndPassword(Email email, Password password);

    Optional<Member> findByEmail(Email payload);

    Optional<Member> findById(Long id);

    List<Member> findAll();
}
