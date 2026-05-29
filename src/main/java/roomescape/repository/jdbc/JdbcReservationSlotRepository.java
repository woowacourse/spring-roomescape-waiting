package roomescape.repository.jdbc;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationSlotRepository;
import roomescape.repository.dto.ReservationCondition;

@Repository
@RequiredArgsConstructor
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private final ReservationSlotDao reservationSlotDao;
    private final ReservationDao reservationDao;

    @Override
    public ReservationSlot save(ReservationSlot slot) {
        Long slotId = saveReservationSlot(slot);
        List<Reservation> savedReservations = reservationDao.saveAll(slotId, slot.getReservations());
        return findById(slotId)
                .orElseGet(() -> new ReservationSlot(
                        slotId,
                        slot.getDate(),
                        slot.getTheme(),
                        slot.getTime(),
                        savedReservations
                ));
    }

    private Long saveReservationSlot(ReservationSlot slot) {
        if (slot.getId() == null) {
            return reservationSlotDao.insert(slot);
        }
        reservationSlotDao.update(slot);
        return slot.getId();
    }

    @Override
    public Optional<ReservationSlot> findById(long id) {
        return reservationSlotDao.findById(id)
                .map(this::withReservations);
    }

    @Override
    public Optional<ReservationSlot> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition) {
        return reservationSlotDao.findByDateAndThemeAndTimeForUpdate(condition)
                .map(this::withReservations);
    }

    @Override
    public Optional<ReservationSlot> findByReservationIdForUpdate(long reservationId) {
        return reservationSlotDao.findByReservationIdForUpdate(reservationId)
                .map(this::withReservations);
    }

    @Override
    public void update(ReservationSlot slot) {
        save(slot);
    }

    private ReservationSlot withReservations(ReservationSlot slot) {
        return new ReservationSlot(
                slot.getId(),
                slot.getDate(),
                slot.getTheme(),
                slot.getTime(),
                reservationDao.findBySlotId(slot.getId())
        );
    }
}
