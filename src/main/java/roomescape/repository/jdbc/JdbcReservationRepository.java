package roomescape.repository.jdbc;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.repository.ReservationRepository;
import roomescape.repository.dto.ReservationCondition;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final ReservationSlotDao reservationSlotDao;
    private final ReservationEntryDao reservationEntryDao;

    @Override
    public Reservation save(Reservation reservation) {
        Long reservationId = saveReservationSlot(reservation);
        List<ReservationEntry> savedEntries = reservationEntryDao.saveAll(reservationId, reservation.getEntries());
        return findById(reservationId)
                .orElseGet(() -> Reservation.restore(
                        reservationId,
                        reservation.getDate(),
                        reservation.getTheme(),
                        reservation.getTime(),
                        savedEntries
                ));
    }

    private Long saveReservationSlot(Reservation reservation) {
        if (reservation.getId() == null) {
            return reservationSlotDao.insert(reservation);
        }
        reservationSlotDao.update(reservation);
        return reservation.getId();
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return reservationSlotDao.findById(id)
                .map(this::withEntries);
    }

    @Override
    public Optional<Reservation> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition) {
        return reservationSlotDao.findByDateAndThemeAndTimeForUpdate(condition)
                .map(this::withEntries);
    }

    @Override
    public Optional<Reservation> findByEntryIdForUpdate(long entryId) {
        return reservationSlotDao.findByEntryIdForUpdate(entryId)
                .map(this::withEntries);
    }

    @Override
    public Optional<Reservation> findByEntryId(long entryId) {
        return reservationSlotDao.findByEntryId(entryId)
                .map(this::withEntries);
    }

    @Override
    public void update(Reservation reservation) {
        save(reservation);
    }

    private Reservation withEntries(Reservation reservation) {
        return Reservation.restore(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getTime(),
                reservationEntryDao.findByReservationId(reservation.getId())
        );
    }
}
