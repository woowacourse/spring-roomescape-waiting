package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.controller.dto.request.ThemeSaveRequest;
import roomescape.reservation.controller.dto.response.ThemeDeleteResponse;
import roomescape.reservation.controller.dto.response.ThemeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ThemeRepository;

@Service
@Transactional
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationService reservationService;

    public ThemeService(ThemeRepository themeRepository, ReservationService reservationService) {
        this.themeRepository = themeRepository;
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
        List<Reservation> reservations = reservationService.findByDateBetween(
                LocalDate.now().minusWeeks(1),
                LocalDate.now()
        );
        List<Long> popularThemes = sortByReservationCountAndLimit(getReservationCountsPerThemeId(reservations));

        return popularThemes.stream()
                .map(id -> ThemeResponse.from(themeRepository.findById(id).get()))
                .toList();
    }

    private Map<Long, List<Reservation>> getReservationCountsPerThemeId(final List<Reservation> reservations) {
        return reservations.stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getTheme().getId()));
    }

    private List<Long> sortByReservationCountAndLimit(final Map<Long, List<Reservation>> collect) {
        return collect.entrySet().stream()
                .sorted((left, right) -> Integer.compare(left.getValue().size(), right.getValue().size()))
                .map(Entry::getKey)
                .limit(10)
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
