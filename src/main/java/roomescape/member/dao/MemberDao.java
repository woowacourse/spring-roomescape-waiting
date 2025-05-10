package roomescape.member.dao;


import roomescape.member.model.Member;

import java.util.List;

public interface MemberDao {

    Member add(Member member);

    List<Member> findAll();

    Member findById(Long memberId);

    Member findByEmail(String email);

    Member findByEmailAndPassword(String email, String password);

    boolean existByEmail(String email);

    boolean existByName(String name);
}
