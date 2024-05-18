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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public Theme save(final ThemeSaveRequest themeSaveRequest) {
        Theme theme = themeSaveRequest.toEntity();
        return themeRepository.save(theme);
    }

    public List<Theme> getAll() {
        return StreamSupport.stream(themeRepository.findAll().spliterator(), false).toList();
    }

    public List<Theme> findPopularThemes() {
        List<Reservation> reservations = reservationRepository.findByDateBetween(
                LocalDate.now().minusDays(8),
                LocalDate.now().minusDays(1)
        );
        List<Long> popularThemeIds = getPopularThemeIds(reservations);

        return popularThemeIds.stream()
                .map(themeRepository::getById)
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

    public int delete(final long id) {
        validateNotExitsThemeById(id);
        validateAlreadyHasReservationByThemeId(id);
        return themeRepository.deleteById(id);
    }

    private void validateNotExitsThemeById(final long id) {
        if (themeRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (themeId : " + id + ") 에 대한 테마가 존재하지 않습니다.");
        }
    }

    private void validateAlreadyHasReservationByThemeId(final long id) {
        if (!reservationRepository.findByThemeId(id).isEmpty()) {
            throw new IllegalArgumentException("[ERROR] 해당 테마를 사용 중인 예약이 있어 삭제할 수 없습니다.");
        }
    }
}
