package roomescape.member.dao;

import roomescape.member.model.Member;

import java.util.List;
import java.util.Optional;

public interface MemberDao {

    Member save(Member member);

    List<Member> findAll();

    Optional<Member> findById(Long memberId);

    Optional<Member> findByEmailAndPassword(String email, String password);

    boolean existsByEmail(String email);

    boolean existsByName(String name);
}
