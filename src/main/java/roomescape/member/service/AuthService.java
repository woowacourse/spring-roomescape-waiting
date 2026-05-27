package roomescape.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.auth.jwt.JwtProvider;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.dto.LoginCommand;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    public String login(LoginCommand command) {
        Member member = getMember(command.name());
        member.matchPassword(command.password());

        return jwtProvider.generateToken(member.getId(), member.getName(), member.getRole());
    }

    private Member getMember(String name) {
        return memberRepository.findByName(name)
            .orElseThrow(IllegalArgumentException::new);
    }

}
