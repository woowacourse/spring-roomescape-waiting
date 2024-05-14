package roomescape.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.auth.TokenProvider;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.exception.InvalidMemberException;
import roomescape.exception.UnauthorizedException;
import roomescape.service.auth.dto.LoginCheckResponse;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.auth.dto.LoginResponse;
import roomescape.service.auth.dto.SignUpRequest;
import roomescape.service.member.dto.MemberResponse;

@Service
public class AuthService {
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @Autowired
    public AuthService(TokenProvider tokenProvider, MemberRepository memberRepository) {
        this.tokenProvider = tokenProvider;
        this.memberRepository = memberRepository;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Member member = memberRepository.getByEmail(loginRequest.email());
        validatePassword(loginRequest, member);
        String token = tokenProvider.create(member);
        return new LoginResponse(token);
    }

    private void validatePassword(LoginRequest request, Member member) {
        if (!member.getPassword().equals(request.password())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 잘못되었습니다.");
        }
    }

    public LoginCheckResponse check(String token) {
        String email = tokenProvider.extractMemberEmail(token);
        Member member = memberRepository.getByEmail(email);
        return new LoginCheckResponse(member);
    }

    public MemberResponse create(SignUpRequest signUpRequest) {
        validateEmail(signUpRequest.email());
        Member member = memberRepository.save(new Member(signUpRequest.name(), signUpRequest.email(), signUpRequest.password(), Role.GUEST));
        return new MemberResponse(member);
    }

    private void validateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new InvalidMemberException("이미 가입된 이메일입니다.");
        }
    }
}
