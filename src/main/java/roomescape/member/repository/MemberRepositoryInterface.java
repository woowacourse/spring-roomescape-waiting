package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;

public interface MemberRepositoryInterface {

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(Long id);

    List<Member> findAll();

    Member save(Member member);

    void deleteById(Long id);

    boolean existsByEmailAndPassword(String email, String password);

    Optional<String> findNameByEmail(String email);
}
