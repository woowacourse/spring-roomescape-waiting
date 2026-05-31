package roomescape.persistence.jdbc;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationSlot;
import roomescape.persistence.ReservationSlotRepository;
import roomescape.persistence.dto.ReservationCondition;
import roomescape.persistence.jdbc.dao.ReservationDao;
import roomescape.persistence.jdbc.dao.ReservationSlotDao;

@Repository
@RequiredArgsConstructor
public class JdbcReservationSlotRepository implements ReservationSlotRepository {

    private final ReservationSlotDao reservationSlotDao;
    private final ReservationDao reservationDao;

    @Override
    public ReservationSlot save(ReservationSlot slot) {
        Long slotId = saveReservationSlot(slot);
        reservationDao.saveAll(slotId, slot.getReservations());
        return findById(slotId)
                .orElseGet(() -> new ReservationSlot(
                        slotId,
                        slot.getDate(),
                        slot.getTheme(),
                        slot.getTime(),
                        slot.getReservations()
                ));
    }

    private Long saveReservationSlot(ReservationSlot slot) {
        if (slot.getId() == null) {
            return reservationSlotDao.insert(slot);
        }
        reservationSlotDao.update(slot);
        return slot.getId();
    }

    private Optional<ReservationSlot> findById(long id) {
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
