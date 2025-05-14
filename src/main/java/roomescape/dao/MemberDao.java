package roomescape.dao;

import java.util.List;
import roomescape.entity.Member;

public interface MemberDao {

    List<Member> findAllMembers();

    Member findMemberByEmail(String email);

    Member findMemberById(Long id);

    boolean existMemberByEmail(String email);

    Member addMember(Member member);
}
