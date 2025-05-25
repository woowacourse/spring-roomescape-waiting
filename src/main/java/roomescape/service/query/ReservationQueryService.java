package roomescape.service.query;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWaitingRank;
import roomescape.domain.reservation.ReservationWaitingTicket;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationWaitingTicketRepository;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationWaitingTicketRepository waitingTicketRepository;

    public ReservationQueryService(JpaReservationRepository reservationRepository,
                                   JpaReservationWaitingTicketRepository waitingTicketRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingTicketRepository = waitingTicketRepository;
    }

    public List<ReservationResponseDto> findAllReservations() {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .map(ReservationResponseDto::of)
                .toList();
    }

    public List<ReservationResponseDto> searchReservationsBy(
            long themeId, long memberId, LocalDate from, LocalDate to
    ) {
        List<Reservation> reservationsByPeriodAndMemberAndTheme = reservationRepository.findReservationsByDateBetweenAndThemeIdAndMemberId(
                from, to, themeId, memberId);
        return reservationsByPeriodAndMemberAndTheme.stream()
                .map(ReservationResponseDto::of)
                .toList();
    }

    public List<ReservationResponseDto> findReservedReservations() {
        return reservationRepository.findReservationsByStatus(ReservationStatus.RESERVED).stream()
                .map(ReservationResponseDto::of)
                .toList();
    }

    public List<ReservationResponseDto> findAllReservationWaitings() {
        return reservationRepository.findReservationsByStatus(ReservationStatus.WAITING).stream()
                .map(ReservationResponseDto::of)
                .toList();
    }

    public List<MyReservationResponseDto> findMyReservations(LoginInfo loginInfo) {
        List<Reservation> reservations = reservationRepository.findReservationsByMemberId(loginInfo.id());
        return reservations.stream().map(reservation -> {
            if (reservation.isReservationWaiting()) {
                ReservationWaitingTicket reservationWaitingTicket = waitingTicketRepository.findByReservationId(
                        reservation.getId());
                ReservationWaitingRank rank = waitingTicketRepository.countReservationWaitingsByThemeIdAndDateAndTimeIdAndCreatedAt(
                        reservation.getTheme().getId(),
                        reservation.getDate(),
                        reservation.getTime().getId(),
                        reservationWaitingTicket.getCreatedAt()
                );
                return new MyReservationResponseDto(
                        reservation, rank
                );
            }
            return new MyReservationResponseDto(reservation);
        }).toList();
    }
}
