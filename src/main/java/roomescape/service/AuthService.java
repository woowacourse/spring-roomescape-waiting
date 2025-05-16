package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.User;
import roomescape.dto.business.TokenInfoDto;
import roomescape.dto.request.loginRequest;
import roomescape.dto.response.TokenResponse;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.UserRepository;
import roomescape.utility.JwtTokenProvider;

@Service
@Transactional
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public TokenResponse login(loginRequest loginRequest) {
        User user = loadUserByEmailAndPassword(loginRequest.email(), loginRequest.password());
        String accessToken = jwtTokenProvider.createToken(new TokenInfoDto(user.getId(), user.getRole()));
        return new TokenResponse(accessToken);
    }

    private User loadUserByEmailAndPassword(String email, String password) {
        return userRepository.findOneByEmailAndPassword(email, password)
                .orElseThrow(NotFoundUserException::new);
    }
}
