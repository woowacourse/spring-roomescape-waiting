package roomescape.member;

import java.util.List;
import java.util.Optional;

public interface MemberRepositoryFacade {

    void save(Member member);
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
    List<Member> findAll();
}
