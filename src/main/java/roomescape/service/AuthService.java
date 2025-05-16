package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.User;
import roomescape.dto.business.TokenInfoDto;
import roomescape.dto.request.TokenRequestDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.dto.response.UserResponseDto;
import roomescape.exception.local.InvalidTokenException;
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
