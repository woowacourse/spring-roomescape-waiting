package roomescape.login;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.Member;
import roomescape.member.application.port.out.MemberRepository;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    public AuthenticatedMember login(String name, String password) {
        Member member = memberRepository.findByName(name)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.LOGIN_FAILED));

        if (!member.isSamePassword(password)) {
            throw new EscapeRoomException(ErrorCode.LOGIN_FAILED);
        }

        return AuthenticatedMember.of(member.getId(), member.getRole());
    }
}
