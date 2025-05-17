package roomescape.auth.sign.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.auth.sign.password.PasswordEncoder;
import roomescape.user.application.dto.SignUpRequest;
import roomescape.user.application.service.UserCommandService;
import roomescape.user.domain.User;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignUpUseCaseImpl implements SignUpUseCase {

    private final PasswordEncoder passwordEncoder;
    private final UserCommandService userCommandService;

    @Override
    public User execute(final SignUpRequest request) {
        final Password encodedPassword = Password.fromRaw(request.rawPassword(), passwordEncoder);

        return userCommandService.create(CreateUserRequest.from(request, encodedPassword));
    }
}
