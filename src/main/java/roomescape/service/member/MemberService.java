package roomescape.service.member;

import java.util.List;
import roomescape.domain.Member;
import roomescape.dto.login.LoginRequest;
import roomescape.dto.member.MemberResponse;
import roomescape.dto.signup.SignupRequest;

public interface MemberService {

    List<MemberResponse> findAllMembers();
    Member findMemberById(Long id);
    MemberResponse addMember(SignupRequest signupRequest);
    String createToken(LoginRequest loginRequest);
}
