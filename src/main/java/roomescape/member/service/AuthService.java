package roomescape.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.auth.dto.LoginMember;
import roomescape.global.auth.util.JwtUtil;
import roomescape.global.error.exception.BadRequestException;
import roomescape.member.dto.request.LoginRequest;
import roomescape.member.entity.Member;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    public String login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("이메일 또는 비밀번호가 일치하지 않습니다."));
        validatePasswordCorrect(request, member);
        LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getRole());
        return jwtUtil.createToken(loginMember);
    }

    private void validatePasswordCorrect(LoginRequest request, Member member) {
        if (!member.matchesPassword(request.password())) {
            throw new BadRequestException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
    }
}
