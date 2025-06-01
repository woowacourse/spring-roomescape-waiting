package roomescape.member.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.application.dto.LoginMemberInfo;
import roomescape.member.application.dto.MemberLoginCommand;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.security.TokenProvider;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public String tokenLogin(final MemberLoginCommand command) {
        final Member loginMember = memberRepository.findByEmailAndPassword(command.email(), command.password())
                .orElseThrow(() -> new RoomescapeException("존재하지 않는 이메일 혹은 비밀번호입니다."));
        return tokenProvider.createToken(String.valueOf(loginMember.id()));
    }

    public LoginMemberInfo getLoginMemberInfoByToken(final String token) {
        final long memberId = Long.parseLong(tokenProvider.parsePayload(token));
        final Member loginMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException("존재하지 않는 회원 정보입니다."));
        return new LoginMemberInfo(loginMember);
    }
}
