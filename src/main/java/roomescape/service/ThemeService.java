package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme.Theme;
import roomescape.dto.theme.ThemeRequest;
import roomescape.dto.theme.ThemeResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.util.DateUtil;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Long addTheme(ThemeRequest themeRequest) {
        validateNameDuplicate(themeRequest.name());
        Theme theme = themeRequest.toEntity();
        return themeRepository.save(theme);
    }

    public List<ThemeResponse> getAllTheme() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public ThemeResponse getTheme(Long id) {
        Theme theme = findThemeById(id);
        return ThemeResponse.from(theme);
    }

    public List<ThemeResponse> getPopularThemes() {
        List<Long> popularThemeIds = reservationRepository.findThemeReservationCountsForDate(
                DateUtil.A_WEEK_AGO, DateUtil.YESTERDAY);
        return popularThemeIds.stream()
                .map(this::findThemeById)
                .map(ThemeResponse::from)
                .toList();
    }

    public void deleteTheme(Long id) {
        Theme theme = findThemeById(id);
        validateDeletable(theme);
        themeRepository.delete(id);
    }

    private Theme findThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 테마 정보 입니다.",
                        new Throwable("theme_id : " + themeId)
                ));
    }

    public void validateNameDuplicate(String name) {
        if (themeRepository.existName(name)) {
            throw new IllegalArgumentException(
                    "[ERROR] 동일한 이름의 테마가 존재해 등록할 수 없습니다.",
                    new Throwable("theme_name : " + name)
            );
        }
    }

    private void validateDeletable(Theme theme) {
        if (reservationRepository.existThemeId(theme.getId())) {
            throw new IllegalArgumentException(
                    "[ERROR] 예약되어있는 테마는 삭제할 수 없습니다.",
                    new Throwable("theme_id : " + theme.getId())
            );
        }
    }
}
