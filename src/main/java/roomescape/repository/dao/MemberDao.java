package roomescape.repository.dao;

import roomescape.model.member.Member;

import java.util.List;
import java.util.Optional;

public interface MemberDao {

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(long id);

    List<Member> findAll();

    Optional<Member> findByEmailAndPassword(String email, String password);
}
