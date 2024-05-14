package roomescape.repository;

import java.util.Optional;
import roomescape.domain.Member;
import roomescape.domain.Members;

public interface MemberRepository {
    Members findAll();

    Optional<Member> findById(long id);

    Optional<Member> findByEmail(String email);

    Member save(Member member);
}
