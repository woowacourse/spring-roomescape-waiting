package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.*;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
    }

    public Long findRankByReservation(Reservation reservation) {
        if (reservation.isBooked()) {
            return 0L;
        }
        WaitingWithRank waitingWithRank = waitingRepository.findByMember(reservation.getMember());
        return waitingWithRank.getRank() + 1;
    }

    public List<Waiting> findAll() {
        return waitingRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        waitingRepository.deleteById(id);
    }

    @Transactional
    public void approveWaitingAsBooking(Reservation deletedReservation) {
        waitingRepository.findFistByDateAndTimeAndThemeOrderByIdAsc(deletedReservation.getDate(), deletedReservation.getTime(), deletedReservation.getTheme())
                .ifPresent(this::changeWaitingToBooking);
    }

    private void changeWaitingToBooking(Waiting waiting) {
        waitingRepository.delete(waiting);
        Reservation reservation = reservationRepository.findByMemberAndDateAndTimeAndTheme(
                waiting.getMember(), waiting.getDate(), waiting.getTime(), waiting.getTheme());
        reservation.setStatus(ReservationStatus.BOOKING);
    }
}
