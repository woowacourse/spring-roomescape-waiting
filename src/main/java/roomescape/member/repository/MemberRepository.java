package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;

public interface MemberRepository extends CrudRepository<Member, Long> {

    List<Member> findAll();

    Optional<Member> findByEmailAndPassword(MemberEmail email, String password);

    boolean existsByEmail(MemberEmail email);

    boolean existsByName(MemberName name);
}
