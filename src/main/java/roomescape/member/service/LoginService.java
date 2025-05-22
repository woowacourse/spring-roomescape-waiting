package roomescape.member.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.TokenResponse;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.jwt.TokenProvider;

@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final TokenProvider jwtTokenProvider;

    public LoginService(MemberRepository memberRepository,
                        TokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse createToken(LoginRequest loginRequest) {
        Optional<Member> memberOptional = memberRepository.findByEmailAndPassword(loginRequest.email(),
                loginRequest.password());
        if (memberOptional.isEmpty()) {
            throw new NotFoundException(ExceptionCause.UNAUTHORIZED_LOGIN_ACCESS);
        }
        String token = jwtTokenProvider.createToken(memberOptional.get());
        return new TokenResponse(token);
    }

    public MemberResponse findMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.UNAUTHORIZED_LOGIN_ACCESS));
        return new MemberResponse(member.getId(), member.getName());
    }
}
