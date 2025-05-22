package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.dto.reservation.ReservationAndWaitingResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.repository.JpaReservationRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final JpaReservationRepository reservationRepository;

    public ReservationQueryService(final JpaReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResponseDto> findAllReservationResponses() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public List<ReservationResponseDto> findReservationBetween(long themeId, long memberId, LocalDate from,
                                                               LocalDate to) {
        List<Reservation> reservationsByPeriodAndMemberAndTheme = reservationRepository.findByPeriod(from, to, themeId, memberId);
        return reservationsByPeriodAndMemberAndTheme.stream()
                .map(reservation -> ReservationResponseDto.of(reservation, reservation.getTime(),
                        reservation.getTheme()))
                .toList();
    }

    public List<ReservationAndWaitingResponseDto> findMyReservations(long memberId) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(memberId);
        return reservations.stream().map(ReservationAndWaitingResponseDto::new).toList();
    }
}
