package roomescape.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.core.token.TokenProvider;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.GetAuthInfoResponse;
import roomescape.auth.dto.response.LoginResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final AuthServiceValidator authServiceValidator;

    public AuthService(MemberRepository memberRepository, TokenProvider tokenProvider,
                       AuthServiceValidator authServiceValidator) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.authServiceValidator = authServiceValidator;
    }

    public LoginResponse login(LoginRequest loginMemberRequest) {
        String email = loginMemberRequest.email();
        Member member = getMember(email);

        authServiceValidator.checkInvalidAuthInfo(member, loginMemberRequest.password());

        return new LoginResponse(tokenProvider.createToken(member));
    }

    @Transactional(readOnly = true)
    public GetAuthInfoResponse getMemberAuthInfo(AuthInfo authInfo) {
        Member member = memberRepository.findById(authInfo.getMemberId())
                .orElseThrow(() -> new SecurityException("회원 정보가 올바르지 않습니다. 회원가입 후 로그인해주세요."));
        return GetAuthInfoResponse.from(member);
    }

    private Member getMember(String email) {
        return memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new IllegalArgumentException("로그인하려는 계정이 존재하지 않습니다. 회원가입 후 로그인해주세요."));
    }
}
