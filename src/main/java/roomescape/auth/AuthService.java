package roomescape.auth;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginRequest;
import roomescape.exception.custom.reason.auth.AuthNotExistsEmailException;
import roomescape.exception.custom.reason.auth.AuthNotValidPasswordException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;

@Service
@AllArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public String generateToken(final LoginRequest loginRequest) {
        final Member memberByEmail = memberRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new AuthNotExistsEmailException());
        validatePassword(loginRequest, memberByEmail);

        return jwtProvider.provideToken(memberByEmail.getEmail(), memberByEmail.getRole(), memberByEmail.getName());
    }

    private void validatePassword(final LoginRequest loginRequest, final Member memberByEmail) {
        if (!memberByEmail.matchesPassword(loginRequest.password())) {
            throw new AuthNotValidPasswordException();
        }
    }
}
