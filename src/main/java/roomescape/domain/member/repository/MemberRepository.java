package roomescape.domain.member.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.member.domain.Member;


public interface MemberRepository {

    Member insert(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmailAndPassword(String email, String password);

    List<Member> findAll();
}
