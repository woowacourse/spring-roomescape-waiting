package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.PopularThemeRequest;
import roomescape.service.dto.request.ThemeSaveRequest;
import roomescape.service.dto.response.ThemeResponse;
import roomescape.service.dto.response.ThemeResponses;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

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

    public ThemeResponses getThemes() {
        return makeThemeResponses(themeRepository.findAll());
    }

    public ThemeResponses getPopularThemes(PopularThemeRequest popularThemeRequest) {
        List<Reservation> reservations = reservationRepository.findAllByDateBetween(
                popularThemeRequest.startDate(),
                popularThemeRequest.endDate()
        );

        List<Theme> popularThemes = makePopularThemeRanking(reservations, popularThemeRequest.limit());

        return makeThemeResponses(popularThemes);
    }

    private ThemeResponses makeThemeResponses(List<Theme> themes) {
        List<ThemeResponse> themeResponses = themes.stream()
                .map(ThemeResponse::new)
                .toList();
        return new ThemeResponses(themeResponses);
    }

    private List<Theme> makePopularThemeRanking(List<Reservation> reservations, int rankingLimit) {
        Map<Theme, Long> reservationCounting = reservations.stream()
                .collect(groupingBy(Reservation::getTheme, Collectors.counting()));

        return reservationCounting.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(rankingLimit)
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
