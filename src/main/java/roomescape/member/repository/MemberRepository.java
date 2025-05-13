package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.member.entity.Member;

public interface MemberRepository {

    Member save(Member member);

    List<Member> findAll();

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    boolean deleteById(Long id);
}
