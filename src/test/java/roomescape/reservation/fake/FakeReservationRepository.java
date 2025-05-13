package roomescape.reservation.fake;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private List<Reservation> data = new ArrayList<>();
    private AtomicLong atomicLong = new AtomicLong();
    private boolean existsByReservationTimeId = false;
    private boolean existsByThemeId = false;

    public FakeReservationRepository(Reservation... inputReservations) {
        data.addAll(List.of(inputReservations));
        long maxId = data.stream()
                .mapToLong(Reservation::getId)
                .max()
                .orElse(0L);
        atomicLong.set(maxId);
    }

    @Override
    public List<Reservation> findByCriteria(
            Long themeId,
            Long memberId,
            LocalDate localDateFrom,
            LocalDate localDateTo
    ) {
        return data;
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(LocalDate date, LocalTime time, Long themeId) {
        return data.stream()
                .anyMatch(reservation -> reservation.getDate().equals(date)
                        && reservation.getTime().getStartAt().equals(time)
                        && reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public Reservation save(Reservation reservation) {
        long newId = atomicLong.incrementAndGet();
        Reservation newReservation = new Reservation(
                newId,
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getMember()
        );
        data.add(newReservation);
        return newReservation;
    }

    @Override
    public int deleteById(Long id) {
        boolean isDeleted = data.removeIf(r -> r.getId().equals(id));
        if (isDeleted) {
            return 1;
        }
        return 0;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return data.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean existsByReservationTimeId(final Long id) {
        return existsByReservationTimeId;
    }

    @Override
    public boolean existsByThemeId(final Long id) {
        return existsByThemeId;
    }

    public void setExistsByReservationTimeId(final boolean existsByReservationTimeId) {
        this.existsByReservationTimeId = existsByReservationTimeId;
    }

    public void setExistsByThemeId(final boolean existsByThemeId) {
        this.existsByThemeId = existsByThemeId;
    }
}
