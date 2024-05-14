package roomescape.member.domain.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberSignUp;

public interface MemberRepository {
    Member save(MemberSignUp memberSignUp);

    Optional<Member> findById(long id);

    Optional<Member> findBy(String email);

    List<Member> findAll();

    boolean existsBy(String email, String password);

    boolean existsBy(String email);
}
