package roomescape.service.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;

@Service
public class ReservationFindService {

    private final ReservationRepository reservationRepository;

    public ReservationFindService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> searchReservations(long memberId, long themeId,
                                                LocalDate dateFrom, LocalDate dateTo) {
        return reservationRepository.findByMemberIdAndThemeIdAndDateBetween(memberId, themeId, dateFrom, dateTo);
    }

    public List<Reservation> findUserReservations(long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }
}
