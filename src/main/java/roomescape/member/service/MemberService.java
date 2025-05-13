package roomescape.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.controller.dto.MemberInfoResponse;
import roomescape.member.controller.dto.SignupRequest;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberId;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.member.service.usecase.MemberCommandUseCase;
import roomescape.member.service.usecase.MemberQueryUseCase;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberCommandUseCase memberCommandUseCase;
    private final MemberQueryUseCase memberQueryUseCase;

    public MemberInfo create(SignupRequest signupRequest) {
        return memberCommandUseCase.create(
                Account.of(
                        Member.withoutId(
                                MemberName.from(signupRequest.name()),
                                MemberEmail.from(signupRequest.email()),
                                Role.MEMBER
                        ),
                        Password.from(signupRequest.password())
                )
        );
    }

    public Account findAccount(LoginRequest loginRequest) {
        return memberQueryUseCase.getAccount(loginRequest);
    }

    public Member get(MemberId id) {
        return memberQueryUseCase.get(id);
    }

    public List<MemberInfoResponse> getAll() {
        return MemberConverter.toResponses(memberQueryUseCase.getAll());
    }
}
