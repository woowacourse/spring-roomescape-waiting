package roomescape.repository;

import roomescape.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    List<Member> findAll();

    Optional<Member> findByEmail(String email);

    Member fetchByEmail(String email);

    Optional<Member> findById(long id);

    Member fetchById(long id);

    Member save(Member member);

    void delete(String email);

}
