package roomescape.repository;

import roomescape.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member user);

    Optional<Member> findByEmailAndPassword(String email, String password);

    Optional<Member> findById(long id);

    List<Member> findAll();

    int deleteById(long id);
}
