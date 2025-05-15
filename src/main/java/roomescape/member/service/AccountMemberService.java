package roomescape.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.controller.dto.MemberInfoResponse;
import roomescape.member.controller.dto.SignupRequest;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.member.service.usecase.AccountCommandUseCase;
import roomescape.member.service.usecase.AccountQueryUseCase;
import roomescape.member.service.usecase.MemberCommandUseCase;
import roomescape.member.service.usecase.MemberQueryUseCase;

@Service
@RequiredArgsConstructor
public class AccountMemberService {

    private final MemberCommandUseCase memberCommandUseCase;
    private final MemberQueryUseCase memberQueryUseCase;
    private final AccountCommandUseCase accountCommandUseCase;
    private final AccountQueryUseCase accountQueryUseCase;

    @Transactional
    public MemberInfo create(SignupRequest signupRequest) {
        Member member = memberCommandUseCase.create(
                Member.withoutId(
                        MemberName.from(signupRequest.name()),
                        MemberEmail.from(signupRequest.email()),
                        Role.MEMBER
                )
        );

        return MemberConverter.toDto(
                accountCommandUseCase.create(
                        Account.withoutId(
                                member,
                                Password.from(signupRequest.password())
                        )
                )
        );
    }

    public Account findAccount(LoginRequest loginRequest) {
        return accountQueryUseCase.getAccount(loginRequest);
    }

    public Member get(Long id) {
        return memberQueryUseCase.get(id);
    }

    public List<MemberInfoResponse> getAll() {
        return MemberConverter.toResponses(memberQueryUseCase.getAll());
    }
}
