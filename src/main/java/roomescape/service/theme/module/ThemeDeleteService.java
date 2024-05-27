package roomescape.service.theme.module;

import org.springframework.stereotype.Service;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ThemeDeleteService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeDeleteService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void deleteTheme(Long themeId) {
        Theme theme = findThemeById(themeId);
        validateDeletable(theme);
        themeRepository.deleteById(themeId);
    }

    private Theme findThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 테마 정보 입니다.",
                        new Throwable("theme_id : " + themeId)
                ));
    }

    private void validateDeletable(Theme theme) {
        if (reservationRepository.existsByThemeId(theme.getId())) {
            throw new IllegalArgumentException(
                    "[ERROR] 예약되어있는 테마는 삭제할 수 없습니다.",
                    new Throwable("theme_id : " + theme.getId())
            );
        }
    }
}
