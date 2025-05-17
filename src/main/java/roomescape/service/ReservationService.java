package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.service.result.ReservationResult;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResult> getReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom,
                                                               LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findReservationsInConditions(memberId, themeId, dateFrom,
                dateTo);
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> getMemberReservationsById(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        return ReservationResult.from(reservations);
    }

    public void deleteById(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
