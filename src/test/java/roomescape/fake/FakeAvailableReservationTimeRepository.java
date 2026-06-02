package roomescape.fake;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.repository.AvailableReservationTime;
import roomescape.reservationtime.domain.repository.AvailableReservationTimeRepository;

@RequiredArgsConstructor
public class FakeAvailableReservationTimeRepository implements AvailableReservationTimeRepository {

    private final FakeReservationTimeRepository timeRepository;
    private final FakeReservationRepository reservationRepository;

    @Override
    public List<AvailableReservationTime> findByThemeAndDate(Long themeId, LocalDate date) {
        Set<Long> reservedTimeIds = reservationRepository.findAllReservations().stream()
                .filter(reservation -> reservation.getThemeId().equals(themeId))
                .filter(reservation -> reservation.getDate().equals(date))
                .map(Reservation::getTimeId)
                .collect(Collectors.toSet());

        return timeRepository.findAll().stream()
                .map(time -> new AvailableReservationTime(
                        time.getId(),
                        time.getStartAt(),
                        !reservedTimeIds.contains(time.getId())
                ))
                .toList();
    }
}
