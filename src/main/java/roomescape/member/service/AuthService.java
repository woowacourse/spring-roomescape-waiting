package roomescape.member.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.jwt.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.RegistrationRequest;
import roomescape.member.repository.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider jwtTokenProvider;

    public AuthService(MemberRepository memberRepository, TokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createToken(LoginRequest loginRequest) {
        Member member = getMember(loginRequest);
        return jwtTokenProvider.createToken(member);
    }

    public void signup(RegistrationRequest registrationRequest) {
        if (memberRepository.existsByEmail(registrationRequest.email())) {
            throw new ConflictException(ExceptionCause.MEMBER_DUPLICATE_EMAIL);
        }
        memberRepository.save(registrationRequest.createMember());
    }

    private Member getMember(LoginRequest loginRequest) {
        Optional<Member> memberOptional = memberRepository.findByEmailAndPassword(loginRequest.email(),
                loginRequest.password());
        if (memberOptional.isEmpty()) {
            throw new NotFoundException(ExceptionCause.UNAUTHORIZED_LOGIN_ACCESS);
        }
        return memberOptional.get();
    }
}
