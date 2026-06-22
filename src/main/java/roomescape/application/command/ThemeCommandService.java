package roomescape.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

@Service
@Transactional
public class ThemeCommandService {

    private final ThemeRepository themeRepository;

    public ThemeCommandService(
            ThemeRepository themeRepository
    ) {
        this.themeRepository = themeRepository;
    }

    public Theme save(Theme theme) {
        return themeRepository.save(theme);
    }

    public void delete(Long id) {
        themeRepository.deleteById(id);
    }
}
