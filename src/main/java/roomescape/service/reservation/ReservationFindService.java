package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWaitingWithRank;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationFindService {

    private final ReservationRepository reservationRepository;

    public ReservationFindService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> searchReservations(long memberId,
                                                long themeId,
                                                LocalDate dateFrom,
                                                LocalDate dateTo) {
        return reservationRepository.findByMemberIdAndThemeIdAndDateBetween(
                memberId,
                themeId,
                dateFrom,
                dateTo
        );
    }

    public List<ReservationWaitingWithRank> findMemberReservations(long memberId) {
        return reservationRepository.findReservationWaitingWithRankByMemberId(memberId);
    }

    public List<Reservation> findWaitings() {
        return reservationRepository.findByReservationStatus(ReservationStatus.WAITING);
    }
}
