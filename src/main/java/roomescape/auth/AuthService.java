package roomescape.auth;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginRequest;
import roomescape.exception.custom.reason.auth.AuthNotExistsEmailException;
import roomescape.exception.custom.reason.auth.AuthNotValidPasswordException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberJpaRepository;

@Service
@AllArgsConstructor
public class AuthService {

    private final MemberJpaRepository memberJpaRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public String generateToken(final LoginRequest loginRequest) {
        final Member member = memberJpaRepository.findByEmail(loginRequest.email())
                .orElseThrow(AuthNotExistsEmailException::new);
        validatePassword(loginRequest, member);

        return jwtProvider.provideToken(member.getEmail(), member.getRole(), member.getName());
    }

    private void validatePassword(final LoginRequest loginRequest, final Member member) {
        if (!passwordEncoder.matches(loginRequest.password(), member.getPassword())) {
            throw new AuthNotValidPasswordException();
        }
    }
}
