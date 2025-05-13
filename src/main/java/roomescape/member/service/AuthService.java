package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberDao;
import roomescape.member.service.dto.MemberLoginCommand;
import roomescape.member.service.dto.LoginMemberInfo;

@Service
public class AuthService {

    private final MemberDao memberDao;
    private final TokenProvider tokenProvider;

    public AuthService(final MemberDao memberDao, final TokenProvider tokenProvider) {
        this.memberDao = memberDao;
        this.tokenProvider = tokenProvider;
    }

    public String tokenLogin(final MemberLoginCommand command) {
        final Member loginMember = memberDao.findByEmailAndPassword(command.email(), command.password())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 혹은 비밀번호입니다."));
        Long memberId = loginMember.getId();
        return tokenProvider.createToken(memberId.toString());
    }

    public LoginMemberInfo getLoginMemberInfoByToken(String token) {
        long memberId = Long.parseLong(tokenProvider.parsePayload(token));
        Member loginMember = memberDao.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 정보입니다."));
        return new LoginMemberInfo(loginMember);
    }
}
