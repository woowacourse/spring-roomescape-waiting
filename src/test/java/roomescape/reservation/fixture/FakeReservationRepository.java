package roomescape.reservation.fixture;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;

import static roomescape.reservation.domain.ReservationStatus.RESERVED;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> store = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public List<Reservation> findAll() {
        return store.values().stream().toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Reservation> findReservedAndWaitingBySlot(Long dateId, Long timeId, Long themeId) {
        return store.values().stream()
                .filter(reservation -> reservation.getDate().getId().equals(dateId))
                .filter(reservation -> reservation.getTime().getId().equals(timeId))
                .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                .filter(reservation ->
                        reservation.getStatus() == RESERVED || reservation.getStatus() == WAITING)
                .sorted(Comparator
                        .comparing((Reservation r) -> r.getDate().getDate(), Comparator.reverseOrder())
                        .thenComparing(r -> r.getTime().getStartAt()))
                .toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        Long id = idGenerator.getAndIncrement();
        Reservation saved = Reservation.load(id, reservation.getName(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme(), reservation.getStatus(), reservation.getReservedAt());
        store.put(id, saved);
        return saved;
    }

    public List<Reservation> saveAll(List<Reservation> reservations) {
        List<Reservation> savedReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            savedReservations.add(save(reservation));
        }
        return savedReservations;
    }

    @Override
    public boolean existsByDateAndTimeAndThemeId(Long dateId, Long timeId, Long themeId) {
        return store.values().stream()
                .anyMatch(reservation ->
                        reservation.getDate().getId().equals(dateId) &&
                                reservation.getTime().getId().equals(timeId) &&
                                reservation.getTheme().getId().equals(themeId) &&
                                reservation.getStatus() == RESERVED
                );
    }

    @Override
    public boolean updateStatus(Reservation reservation) {
        Optional<Reservation> findReservation = findById(reservation.getId());
        if (findReservation.isEmpty()) {
            return false;
        }

        findReservation.get().updateStatus(reservation.getStatus());
        return true;
    }

    @Override
    public boolean updateSchedule(Reservation reservation) {
        Optional<Reservation> findReservation = findById(reservation.getId());
        if (findReservation.isEmpty()) {
            return false;
        }

        store.put(reservation.getId(), reservation);
        return true;
    }

    @Override
    public List<ReservationWithWaitingTurn> findMyReservationsWithWaitingTurn(String memberName) {
        return List.of();
    }

}
