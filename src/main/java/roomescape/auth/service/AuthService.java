package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoggedInMember;
import roomescape.auth.dto.LoginRequest;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;

@Service
public class AuthService {
    private final TokenProvider tokenProvider;
    private final MemberDao memberDao;

    public AuthService(TokenProvider tokenProvider, MemberDao memberDao) {
        this.tokenProvider = tokenProvider;
        this.memberDao = memberDao;
    }

    public String createToken(LoginRequest request) {
        Member member = memberDao.findMemberByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 존재하지 않습니다."));
        return tokenProvider.createToken(member.getId());
    }

    public LoggedInMember findLoggedInMember(String token) {
        Long memberId = tokenProvider.findMemberId(token);
        Member member = memberDao.findMemberById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 존재하지 않습니다."));
        return LoggedInMember.from(member);
    }
}
