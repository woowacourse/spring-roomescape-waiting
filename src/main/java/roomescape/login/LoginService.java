package roomescape.login;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.AuthenticatedMember;
import roomescape.member.Member;
import roomescape.member.infrastructure.MemberRepository;

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
