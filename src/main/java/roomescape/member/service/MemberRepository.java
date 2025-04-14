package roomescape.member.service;

import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;

public interface MemberRepository {
    Member save(Name name, Email email, Password password);
    boolean isExistUser(String email, String password);
    Member findUserByEmail(String payload);
    Member findMemberById(Long id);
    Member findMemberByName(String name);
}
