package roomescape.global.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.auth.domain.dto.TokenInfoDto;
import roomescape.global.auth.domain.dto.TokenRequestDto;
import roomescape.global.auth.domain.dto.TokenResponseDto;
import roomescape.global.auth.exception.InvalidTokenException;
import roomescape.user.domain.User;
import roomescape.user.domain.dto.UserResponseDto;
import roomescape.user.exception.NotFoundUserException;
import roomescape.user.repository.UserRepository;

@Service
@Transactional
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public TokenResponseDto login(TokenRequestDto tokenRequestDto) {
        User user = userRepository.findOneByEmailAndPassword(tokenRequestDto.email(), tokenRequestDto.password())
                .orElseThrow(NotFoundUserException::new);
        return createToken(TokenInfoDto.of(user));
    }

    public TokenResponseDto createToken(TokenInfoDto tokenInfoDto) {
        String accessToken = jwtTokenProvider.createToken(tokenInfoDto);
        return new TokenResponseDto(accessToken);
    }

    public UserResponseDto findMemberByToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new InvalidTokenException();
        }
        String payload = jwtTokenProvider.getPayload(token);
        User user = findMember(payload);
        return UserResponseDto.of(user);
    }

    public User findMember(String payload) {
        Long id = Long.valueOf(payload);
        return userRepository.findById(id)
                .orElseThrow(NotFoundUserException::new);
    }
}
