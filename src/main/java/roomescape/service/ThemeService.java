package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.PopularThemeRequest;
import roomescape.service.dto.ThemeResponse;
import roomescape.service.dto.ThemeSaveRequest;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse saveTheme(ThemeSaveRequest themeSaveRequest) {
        Theme theme = themeSaveRequest.toTheme();
        Theme savedTheme = themeRepository.save(theme);
        return new ThemeResponse(savedTheme);
    }

    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> getPopularThemes(PopularThemeRequest popularThemeRequest) {
        return reservationRepository.findTopThemesDurationOrderByCount(popularThemeRequest.startDate(), popularThemeRequest.endDate(), popularThemeRequest.limit())
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public void deleteTheme(Long id) {
        validateDeleteTheme(id);

        themeRepository.deleteById(id);
    }

    private void validateDeleteTheme(Long id) {
        Theme foundTheme = themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));

        if (reservationRepository.existByTheme(foundTheme)) {
            throw new RoomEscapeBusinessException("예약이 존재하는 테마입니다.");
        }

    }
}
