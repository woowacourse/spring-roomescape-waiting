package roomescape.dao;


import roomescape.domain.Member;

public interface MemberDao {

    Member add(Member member);

    Member findByEmail(String email);

    Member findByEmailAndPassword(String email, String password);

    boolean existByEmail(String email);

    boolean existByName(String name);
}
