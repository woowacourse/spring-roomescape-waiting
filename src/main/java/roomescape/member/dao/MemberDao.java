package roomescape.member.dao;

import java.util.List;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRegistrationInfo;

public interface MemberDao {

    Member save(MemberRegistrationInfo memberRegistrationInfo);

    List<Member> findAll();

    Member findById(long id);

    Member findByEmail(String email);

    void delete(long id);

}
