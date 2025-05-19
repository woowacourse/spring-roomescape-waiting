package roomescape.application.reservation.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.command.dto.CreateThemeCommand;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.infrastructure.error.exception.ThemeException;

@Service
@Transactional
public class CreateThemeService {

    private final ThemeRepository themeRepository;

    public CreateThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Long register(CreateThemeCommand createThemeCommand) {
        if (themeRepository.existsByName(createThemeCommand.name())) {
            throw new ThemeException("이미 같은 이름의 테마가 존재합니다.");
        }
        Theme theme = themeRepository.save(
                new Theme(
                        createThemeCommand.name(),
                        createThemeCommand.description(),
                        createThemeCommand.thumbnail()
                )
        );
        return theme.getId();
    }
}
