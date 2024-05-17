package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.request.ThemeSaveRequest;
import roomescape.reservation.controller.dto.response.ThemeDeleteResponse;
import roomescape.reservation.controller.dto.response.ThemeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ReservationService reservationService;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository,
                        ReservationService reservationService) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    public ThemeResponse save(final ThemeSaveRequest themeSaveRequest) {
        Theme theme = themeSaveRequest.toEntity();
        return ThemeResponse.from(themeRepository.save(theme));
    }

    public List<ThemeResponse> getAll() {
        return StreamSupport.stream(themeRepository.findAll().spliterator(), false)
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findPopularThemes() {
        List<Reservation> reservations = reservationRepository.findByDateBetween(
                LocalDate.now().minusDays(8),
                LocalDate.now().minusDays(1)
        );
        List<Long> popularThemeIds = getPopularThemeIds(reservations);

        return popularThemeIds.stream()
                .map(id -> ThemeResponse.from(themeRepository.findById(id).get()))
                .toList();
    }

    private List<Long> getPopularThemeIds(final List<Reservation> reservations) {
        return reservations.stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getTheme().getId()))
                .entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().size()))
                .limit(10)
                .map(Entry::getKey)
                .toList();
    }

    public ThemeDeleteResponse delete(final long id) {
        validateNotExitsThemeById(id);
        reservationService.validateAlreadyHasReservationByThemeId(id);
        return new ThemeDeleteResponse(themeRepository.deleteById(id));
    }

    private void validateNotExitsThemeById(final long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (id : " + id + ") 에 대한 테마가 존재하지 않습니다.");
        }
    }
}
