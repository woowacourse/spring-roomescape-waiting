package roomescape.service.login;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.MemberRole;
import roomescape.exception.login.InvalidTokenException;
import roomescape.exception.login.UnauthorizedEmailException;
import roomescape.exception.login.UnauthorizedPasswordException;
import roomescape.exception.member.DuplicatedMemberEmailException;
import roomescape.service.login.dto.LoginCheckResponse;
import roomescape.service.login.dto.LoginRequest;
import roomescape.service.login.dto.SignupRequest;
import roomescape.service.login.dto.SignupResponse;

@Service
@Transactional
public class LoginService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginService(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.toMemberEmail())
                .orElseThrow(UnauthorizedEmailException::new);
        if (member.isDifferentPassword(request.toMemberPassword())) {
            throw new UnauthorizedPasswordException();
        }
        return jwtTokenProvider.createToken(member.getEmail(), member.getRole());
    }

    @Transactional(readOnly = true)
    public LoginCheckResponse loginCheck(Member member) {
        return new LoginCheckResponse(member);
    }

    @Transactional(readOnly = true)
    public MemberRole findMemberRoleByToken(String token) {
        return jwtTokenProvider.getMemberRole(token);
    }

    @Transactional(readOnly = true)
    public Member findMemberByToken(String token) {
        MemberEmail email = jwtTokenProvider.getMemberEmail(token);
        return memberRepository.findByEmail(email)
                .orElseThrow(InvalidTokenException::new);
    }

    public SignupResponse signup(SignupRequest request) {
        validateDuplicateEmail(request.toMemberEmail());
        Member member = request.toMember(MemberRole.USER);
        Member savedMember = memberRepository.save(member);
        return new SignupResponse(savedMember);
    }

    private void validateDuplicateEmail(MemberEmail email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicatedMemberEmailException();
        }
    }
}
