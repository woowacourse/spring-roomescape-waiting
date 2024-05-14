package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.GetAuthInfoResponse;
import roomescape.auth.dto.response.LoginResponse;
import roomescape.auth.core.token.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public AuthService(final MemberRepository memberRepository, final TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse login(final LoginRequest loginMemberRequest) {
        String email = loginMemberRequest.email();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인하려는 계정이 존재하지 않습니다. 회원가입 후 로그인해주세요."));
        checkInvalidAuthInfo(member, loginMemberRequest.password());
        return new LoginResponse(tokenProvider.createToken(member));
    }

    private void checkInvalidAuthInfo(final Member member, final String password) {
        if (member.hasNotSamePassword(password)) {
            throw new IllegalArgumentException("아이디 또는 비밀번호를 잘못 입력했습니다. 다시 입력해주세요.");
        }
    }

    public GetAuthInfoResponse getMemberAuthInfo(final AuthInfo authInfo) {
        Member member =  memberRepository.findById(authInfo.getMemberId())
                .orElseThrow(() -> new SecurityException("회원 정보가 올바르지 않습니다. 회원가입 후 로그인해주세요."));
        return GetAuthInfoResponse.from(member);
    }
}
