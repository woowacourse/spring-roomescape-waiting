package roomescape.dao;


import roomescape.domain.Member;

public interface MemberDao {

    Member findByEmailAndPassword(String email, String password);
}
