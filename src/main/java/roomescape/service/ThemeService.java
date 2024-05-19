package roomescape.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.PopularThemeRequest;
import roomescape.service.dto.response.ThemeResponse;
import roomescape.service.dto.request.ThemeSaveRequest;

import static java.util.stream.Collectors.groupingBy;

@Service
public class ThemeService {

    private static final int POPULAR_THEME_RANK_COUNT = 10;

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
        List<Reservation> reservations = reservationRepository.findAllByDateBetween(
                popularThemeRequest.startDate(),
                popularThemeRequest.endDate()
        );

        List<Theme> popularThemes = makePopularThemeRanking(reservations);

        return popularThemes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    private List<Theme> makePopularThemeRanking(List<Reservation> reservations) {
        Map<Theme, Long> reservationCounting = reservations.stream()
                .collect(groupingBy(Reservation::getTheme, Collectors.counting()));

        return reservationCounting.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(POPULAR_THEME_RANK_COUNT)
                .map(Map.Entry::getKey)
                .toList();
    }

    public void deleteTheme(Long id) {
        Theme foundTheme = themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));

        if (reservationRepository.existsByTheme(foundTheme)) {
            throw new RoomEscapeBusinessException("예약이 존재하는 테마입니다.");
        }
        themeRepository.delete(foundTheme);
    }
}
