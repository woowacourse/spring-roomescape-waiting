package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;
import roomescape.exception.login.InvalidTokenException;
import roomescape.exception.login.UnauthorizedEmailException;
import roomescape.exception.login.UnauthorizedPasswordException;
import roomescape.service.dto.request.LoginRequest;
import roomescape.service.dto.request.SignupRequest;
import roomescape.service.dto.response.LoginCheckResponse;
import roomescape.service.dto.response.SignupResponse;
import roomescape.service.helper.JwtTokenProvider;

@Service
@Transactional(readOnly = true)
public class LoginService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginService(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(UnauthorizedEmailException::new);
        if (!member.getPassword().equals(request.password())) {
            throw new UnauthorizedPasswordException();
        }
        return jwtTokenProvider.createToken(member.getEmail(), member.getRole());
    }

    public LoginCheckResponse loginCheck(Member member) {
        return new LoginCheckResponse(member);
    }

    public MemberRole findMemberRoleByToken(String token) {
        return jwtTokenProvider.getMemberRole(token);
    }

    public Member findMemberByToken(String token) {
        String email = jwtTokenProvider.getMemberEmail(token);
        return findMemberByEmail(email);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(InvalidTokenException::new);
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        Member member = request.toMember(MemberRole.USER);
        Member savedMember = memberRepository.save(member);
        return new SignupResponse(savedMember);
    }
}
