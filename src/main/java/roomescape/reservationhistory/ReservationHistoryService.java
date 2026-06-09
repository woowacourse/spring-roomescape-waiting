package roomescape.reservationhistory;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.Reservation;

@Service
@Transactional(readOnly = true)
public class ReservationHistoryService {

    private final ReservationHistoryDao reservationHistoryDao;

    public ReservationHistoryService(ReservationHistoryDao reservationHistoryDao) {
        this.reservationHistoryDao = reservationHistoryDao;
    }

    public List<ReservationHistory> getMyHistory(Long memberId) {
        return reservationHistoryDao.findByMemberId(memberId);
    }

    public List<ReservationHistory> getStoreHistory(Long storeId) {
        return reservationHistoryDao.findByStoreId(storeId);
    }

    @Transactional
    public void recordCreated(Reservation reservation, Long actorId) {
        reservationHistoryDao.insert(
                ReservationHistory.of(reservation, ReservationHistoryAction.CREATED, actorId));
    }

    @Transactional
    public void recordUpdated(Reservation reservation, Long actorId) {
        reservationHistoryDao.insert(
                ReservationHistory.of(reservation, ReservationHistoryAction.UPDATED, actorId));
    }

    @Transactional
    public void recordCanceled(Reservation reservation, Long actorId) {
        reservationHistoryDao.insert(
                ReservationHistory.of(reservation, ReservationHistoryAction.CANCELED, actorId));
    }

    @Transactional
    public void recordTransfer(Reservation transferredOut, Reservation transferredIn, Long actorId) {
        reservationHistoryDao.insert(
                ReservationHistory.of(transferredOut, ReservationHistoryAction.TRANSFERRED_OUT, actorId));
        reservationHistoryDao.insert(
                ReservationHistory.of(transferredIn, ReservationHistoryAction.TRANSFERRED_IN, actorId));
    }
}
