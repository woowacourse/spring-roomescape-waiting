package roomescape.repository.fake;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;

public class FakeReservationTimeRepository implements ReservationTimeRepository {

    private final Map<Long, ReservationTime> data = new HashMap<>();
    private final ReservationRepository reservationRepository;
    private Long autoIncrementId = 1L;

    public FakeReservationTimeRepository(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime reservationTimeToSave = ReservationTime.generateWithPrimaryKey(reservationTime,
                autoIncrementId);
        data.put(autoIncrementId, reservationTimeToSave);
        autoIncrementId++;
        return reservationTimeToSave;
    }

    @Override
    public List<ReservationTime> findAll() {
        return List.copyOf(data.values());
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        data.remove(id);
    }

    @Override
    public List<ReservationTime> findByReservationDateAndThemeId(LocalDate date, Long themeId) {
        if (date == null || themeId == null) {
            return List.of();
        }
        List<Reservation> allReservation = reservationRepository.findAll();
        List<ReservationTime> unavailableTimes = allReservation.stream()
                .filter(reservation -> reservation.getDate().equals(date) && reservation.getTheme().getId().equals(themeId))
                .map(Reservation::getTime)
                .toList();
        List<ReservationTime> availableReservationTime = data.values().stream()
                .filter(time-> !unavailableTimes.contains(time))
                .toList();
        return availableReservationTime;
    }
}
