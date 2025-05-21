package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.dto.LoginMemberInfo;
import roomescape.member.service.dto.MemberLoginCommand;
import roomescape.member.service.dto.TokenInfo;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public AuthService(final MemberRepository memberRepository, final TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public String tokenLogin(final MemberLoginCommand command) {
        final Member loginMember = memberRepository.findByEmailAndPassword(command.email(), command.password())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 혹은 비밀번호입니다."));
        return tokenProvider.createToken(loginMember);
    }

    public LoginMemberInfo getLoginMemberInfoByToken(final String token) {
        TokenInfo tokenInfo = tokenProvider.parsePayload(token);
        Member loginMember = memberRepository.findById(tokenInfo.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 정보입니다."));
        return new LoginMemberInfo(loginMember);
    }

    public TokenInfo getTokenInfo(String token) {
        return tokenProvider.parsePayload(token);
    }
}
