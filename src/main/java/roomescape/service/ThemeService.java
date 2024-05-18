package roomescape.service;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.slot.Theme;
import roomescape.domain.reservation.slot.ThemeRepository;
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

    @Transactional
    public ThemeResponse saveTheme(ThemeSaveRequest themeSaveRequest) {
        Theme theme = themeSaveRequest.toTheme();
        Theme savedTheme = themeRepository.save(theme);
        return new ThemeResponse(savedTheme);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getPopularThemes(PopularThemeRequest popularThemeRequest) {
        List<Theme> popularThemes = reservationRepository.findTopThemesDurationOrderByCount(
                popularThemeRequest.startDate(),
                popularThemeRequest.endDate(),
                Limit.of(popularThemeRequest.limit())
        );

        return popularThemes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    @Transactional
    public void deleteTheme(Long id) {
        Theme foundTheme = themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));

        if (reservationRepository.existsBySlot_Theme(foundTheme)) {
            throw new RoomEscapeBusinessException("예약이 존재하는 테마입니다.");
        }
        themeRepository.delete(foundTheme);
    }
}
