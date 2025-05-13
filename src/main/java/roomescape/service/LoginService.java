package roomescape.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.LoginMember;
import roomescape.dto.member.LoginMemberResponse;
import roomescape.dto.member.LoginRequest;
import roomescape.dto.member.TokenResponse;
import roomescape.exception.InvalidAuthorizationException;
import roomescape.repository.MemberRepository;
import roomescape.util.TokenProvider;

@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final TokenProvider jwtTokenProvider;

    public LoginService(MemberRepository memberRepository, MemberService memberService, TokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse createToken(LoginRequest loginRequest) {
        if (checkInvalidLogin(loginRequest.email(), loginRequest.password())) {
            throw new InvalidAuthorizationException("[ERROR] 로그인 정보를 다시 확인해 주세요.");
        }

        String token = jwtTokenProvider.createToken(loginRequest.email());
        return new TokenResponse(token);
    }

    private boolean checkInvalidLogin(String email, String password) {
        Optional<LoginMember> member = memberRepository.findByEmailAndPassword(email, password);
        return member.map(value -> !email.equals(value.getEmail()) || !password.equals(value.getPassword()))
                .orElse(true);
    }

    public LoginMemberResponse findMemberByToken(String token) {
        LoginMember loginMember = memberService.findMemberByToken(token);
        return new LoginMemberResponse(loginMember.getId(), loginMember.getName());
    }
}
