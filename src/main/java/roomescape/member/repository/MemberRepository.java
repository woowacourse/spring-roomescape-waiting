package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;

public interface MemberRepository {

    Member save(Member member);

    List<Member> findAll();

    Optional<Member> findById(long id);

    Optional<Member> findByEmailAndPassword(MemberEmail email, String password);

    boolean existsByEmail(MemberEmail email);

    boolean existsByName(MemberName name);
}
