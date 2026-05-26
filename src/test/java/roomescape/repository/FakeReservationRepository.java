package roomescape.repository;

import static roomescape.repository.FakeReservationTimeRepository.RESERVATION_TIME_TABLE;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

public class FakeReservationRepository implements ReservationRepository {

    public static final String RESERVATION_TABLE = "reservation";

    private final FakeDatabase fakeDatabase;
    private Long currentId = 0L;

    public FakeReservationRepository(FakeDatabase fakeDatabase) {
        this.fakeDatabase = fakeDatabase;
    }

    @Override
    public Reservation create(Reservation reservationWithoutId) {
        Reservation reservation = Reservation.of(++currentId, reservationWithoutId);
        fakeDatabase.create(RESERVATION_TABLE, reservation.getId(), reservation);
        return reservation;
    }

    @Override
    public Optional<Reservation> readById(Long id) {
        return Optional.ofNullable(fakeDatabase.read(RESERVATION_TABLE, id, Reservation.class));
    }

    @Override
    public List<Reservation> readByName(String name) {
        return fakeDatabase.readAll(RESERVATION_TABLE, Reservation.class).stream()
                .filter(reservation -> reservation.getName().equals(name))
                .toList();
    }

    @Override
    public List<Reservation> readAll() {
        return fakeDatabase.readAll(RESERVATION_TABLE, Reservation.class).stream()
                .toList();
    }

    @Override
    public void update(Long id, LocalDate newDate, Long newTimeId) {
        Reservation reservation = fakeDatabase.read(RESERVATION_TABLE, id, Reservation.class);
        ReservationTime newReservationTime = fakeDatabase.read(RESERVATION_TIME_TABLE, newTimeId,
                ReservationTime.class);

        Reservation updatedReservation = new Reservation(id, reservation.getName(), newDate, newReservationTime,
                reservation.getTheme());

        fakeDatabase.create(RESERVATION_TABLE, updatedReservation.getId(), Reservation.class);
    }

    @Override
    public void delete(Long id) {
        fakeDatabase.delete(RESERVATION_TABLE, id);
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        List<Reservation> reservations = fakeDatabase.readAll(RESERVATION_TABLE, Reservation.class);

        return reservations.stream()
                .anyMatch(reservation -> reservation.getDate().equals(date)
                        && reservation.getTime().getId().equals(timeId)
                        && reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public boolean existByTimeId(Long timeId) {
        List<Reservation> reservations = fakeDatabase.readAll(RESERVATION_TABLE, Reservation.class);

        return reservations.stream()
                .anyMatch(reservation -> reservation.getTime().getId().equals(timeId));
    }

    @Override
    public boolean existByThemeId(Long themeId) {
        List<Reservation> reservations = fakeDatabase.readAll(RESERVATION_TABLE, Reservation.class);

        return reservations.stream()
                .anyMatch(reservation -> reservation.getTheme().getId().equals(themeId));

    }
}
