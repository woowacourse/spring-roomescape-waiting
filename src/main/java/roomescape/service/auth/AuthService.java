package roomescape.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.auth.TokenProvider;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Password;
import roomescape.exception.ForbiddenException;
import roomescape.exception.InvalidMemberException;
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
        Member member = getByEmail(loginRequest.email());
        validatePassword(loginRequest, member);
        String token = tokenProvider.create(member);
        return new LoginResponse(token);
    }

    private void validatePassword(LoginRequest request, Member member) {
        if (!member.isPasswordMatches(Password.of(request.password()))) {
            throw new ForbiddenException("이메일 또는 비밀번호가 잘못되었습니다.");
        }
    }

    public LoginCheckResponse check(String token) {
        String email = tokenProvider.extractMemberEmail(token);
        Member member = getByEmail(email);
        return new LoginCheckResponse(member);
    }

    private Member getByEmail(String email) {
        return memberRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new InvalidMemberException("이메일 또는 비밀번호가 잘못되었습니다."));
    }

    public MemberResponse create(SignUpRequest signUpRequest) {
        Member member = signUpRequest.toMember();
        validateEmail(member.getEmail());
        Member newMember = memberRepository.save(member);
        return new MemberResponse(newMember);
    }

    private void validateEmail(Email email) {
        if (memberRepository.existsByEmail(email)) {
            throw new InvalidMemberException("이미 가입된 이메일입니다.");
        }
    }
}
