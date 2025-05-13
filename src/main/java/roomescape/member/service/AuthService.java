package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.dto.LoginMemberInfo;
import roomescape.member.service.dto.MemberLoginCommand;

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
        Long memberId = loginMember.getId();
        return tokenProvider.createToken(memberId.toString());
    }

    public LoginMemberInfo getLoginMemberInfoByToken(String token) {
        long memberId = Long.parseLong(tokenProvider.parsePayload(token));
        Member loginMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 정보입니다."));
        return new LoginMemberInfo(loginMember);
    }
}
