package roomescape.unit.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.dto.response.AvailableTimeResponse;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final List<ReservationTime> reservationTimes = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);
    private final ReservationRepository reservationRepository;

    public FakeReservationTimeRepository(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<ReservationTime> findAll() {
        return new ArrayList<>(reservationTimes);
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime reservationTimeWithId = new ReservationTime(index.getAndIncrement(),
                reservationTime.getStartAt());
        reservationTimes.add(reservationTimeWithId);
        return reservationTimeWithId;
    }

    @Override
    public void deleteById(Long id) {
        reservationTimes.removeIf(reservationTime -> reservationTime.getId().equals(id));
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> reservationTime.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<AvailableTimeResponse> findByDateAndThemeIdWithBooked(LocalDate date, Long themeId) {
        List<ReservationTime> nonAvailableReservations = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                .map(Reservation::getReservationTime)
                .toList();

        return reservationTimes.stream()
                .map(t -> new AvailableTimeResponse(t.getId(), t.getStartAt(), nonAvailableReservations.contains(t)))
                .toList();
    }
}
