package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeCommandService {

    private final ThemeRepository themeRepository;

    public ThemeCommandService(
            ThemeRepository themeRepository
    ) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Theme save(Theme theme) {
        return themeRepository.save(theme);
    }

    @Transactional
    public void delete(Long id) {
        themeRepository.deleteById(id);
    }
}
