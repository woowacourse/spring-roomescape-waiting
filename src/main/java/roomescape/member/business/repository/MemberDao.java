package roomescape.member.business.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.business.domain.Member;

public interface MemberDao {

    List<Member> findAll();

    Member save(Member member);

    int deleteById(Long id);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);
}
