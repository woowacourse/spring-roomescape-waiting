package roomescape.theme.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ThemeResponse createTheme(ThemeCreateRequest request) {
        Theme theme = themeRepository.save(request.toTheme());
        return ThemeResponse.from(theme);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> findAll() {
        List<Theme> themes = themeRepository.findAll();
        return convertThemesToThemeResponses(themes);
    }

    @Transactional
    public void deleteThemeById(Long id) {
        themeRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public List<ThemeResponse> findPopularThemesByDesc() {
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now().minusDays(1);
        List<Theme> themes = themeRepository.findTopByReservationCountDesc(fromDate, toDate, 10L);
        return convertThemesToThemeResponses(themes);
    }

    private List<ThemeResponse> convertThemesToThemeResponses(List<Theme> themes) {
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Theme getByThemeId(Long id) {
        return themeRepository.findById(id).orElseThrow(() -> new BadRequestException("올바른 방탈출 테마가 없습니다."));
    }
}
