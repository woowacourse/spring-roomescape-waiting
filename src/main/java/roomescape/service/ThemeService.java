package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.util.DateUtil;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Long addTheme(ThemeRequest themeRequest) {
        ThemeName name = new ThemeName(themeRequest.name());
        validateNameDuplicate(name);
        Theme theme = themeRequest.toEntity();

        return themeRepository.save(theme).getId();
    }

    public void validateNameDuplicate(ThemeName name) {
        if (themeRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "[ERROR] 동일한 이름의 테마가 존재해 등록할 수 없습니다.",
                    new Throwable("theme_name : " + name)
            );
        }
    }

    @Transactional
    public void deleteTheme(Long id) {
        Theme theme = themeRepository.getById(id);
        validateDeletable(theme);
        themeRepository.deleteById(id);
    }

    private void validateDeletable(Theme theme) {
        if (reservationRepository.existsByThemeId(theme.getId())) {
            throw new IllegalArgumentException(
                    "[ERROR] 예약되어있는 테마는 삭제할 수 없습니다.",
                    new Throwable("theme_id : " + theme.getId())
            );
        }
    }

    public List<ThemeResponse> getAllTheme() {
        List<Theme> themes = themeRepository.findAll();

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse getTheme(Long id) {
        Theme theme = themeRepository.getById(id);

        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> getPopularThemes() {
        List<Long> popularThemeIds = reservationRepository.findThemeReservationCountsForDate(
                DateUtil.aWeekAgo(),
                DateUtil.yesterday()
        );

        return themeRepository.findAllById(popularThemeIds)
                .stream().map(ThemeResponse::from)
                .toList();
    }
}
