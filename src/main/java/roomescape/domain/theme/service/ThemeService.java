package roomescape.domain.theme.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.dto.ThemeAddRequest;
import roomescape.domain.theme.repository.ThemeRepository;

import java.util.List;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> findAllTheme() {
        return themeRepository.findAll();
    }

    public Theme addTheme(ThemeAddRequest themeAddRequest) {
        Theme theme = themeAddRequest.toEntity();
        return themeRepository.save(theme);
    }

    public void removeTheme(Long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("해당 id를 가진 테마가 존재하지 않습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<Theme> getThemeRanking() {
        return themeRepository.findThemeOrderByReservationCount();
    }
}
