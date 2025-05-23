package roomescape.member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    void save(Member member);

    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);
    List<Member> findAll();

    boolean existsByEmail(String email);
}
