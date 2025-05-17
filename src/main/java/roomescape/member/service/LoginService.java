package roomescape.member.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.common.exception.LoginException;
import roomescape.common.util.DateTime;
import roomescape.common.util.JwtTokenContainer;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.dto.request.LoginMember;
import roomescape.member.dto.request.LoginRequest;

@Service
public class LoginService {

    private final JwtTokenContainer jwtTokenContainer;
    private final MemberRepository memberRepository;
    private final DateTime dateTime;

    public LoginService(final JwtTokenContainer jwtTokenContainer,
                        final MemberRepository memberRepository,
                        final DateTime dateTime) {
        this.jwtTokenContainer = jwtTokenContainer;
        this.memberRepository = memberRepository;
        this.dateTime = dateTime;
    }

    public String loginAndReturnToken(LoginRequest request) {
        Optional<Member> loginMember = memberRepository.findByEmailAndPassword(request.email(), request.password());
        if (loginMember.isEmpty()) {
            throw new LoginException("아이디 혹은 비밀번호가 일치하지 않습니다.");
        }
        return jwtTokenContainer.createJwtToken(loginMember.get(), dateTime.now());
    }

    public LoginMember loginCheck(String token) {
        jwtTokenContainer.validateToken(token);
        Long memberId = jwtTokenContainer.getMemberId(token);
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isEmpty()) {
            throw new LoginException("유효하지 않은 회원입니다.");
        }
        Member findMember = member.get();
        return new LoginMember(memberId, findMember.getName());
    }
}
