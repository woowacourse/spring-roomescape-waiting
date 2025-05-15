package roomescape.auth.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;
import roomescape.exception.InvalidAuthorizationException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.util.TokenProvider;

@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final TokenProvider jwtTokenProvider;

    public LoginService(MemberRepository memberRepository, MemberService memberService,
                        TokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse createToken(LoginRequest loginRequest) {
        Optional<Member> memberOptional = memberRepository.findByEmailAndPassword(loginRequest.email(),
                loginRequest.password());
        if (memberOptional.isEmpty()) {
            throw new InvalidAuthorizationException("[ERROR] 로그인 정보를 다시 확인해 주세요.");
        }
        String token = jwtTokenProvider.createToken(memberOptional.get());
        return new TokenResponse(token);
    }

    public MemberResponse findMemberByToken(String token) {
        Member member = memberService.findMemberByToken(token);
        return new MemberResponse(member.getId(), member.getName());
    }
}
