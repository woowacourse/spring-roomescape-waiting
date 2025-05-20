package roomescape.business.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.AuthToken;
import roomescape.auth.jwt.JwtUtil;
import roomescape.business.model.repository.Users;
import roomescape.exception.auth.AuthenticationException;

import static roomescape.exception.SecurityErrorCode.INVALID_EMAIL;
import static roomescape.exception.SecurityErrorCode.INVALID_PASSWORD;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final Users users;
    private final JwtUtil jwtUtil;

    public AuthToken authenticate(final String email, final String password) {
        val user = users.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException(INVALID_EMAIL));

        if (!user.isPasswordCorrect(password)) {
            throw new AuthenticationException(INVALID_PASSWORD);
        }

        return jwtUtil.createToken(user);
    }
}
