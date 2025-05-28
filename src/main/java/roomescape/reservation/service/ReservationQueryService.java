package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.auth.LoginMember;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationQueryService(final ReservationRepository reservationRepository,
                                   final WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> getReservations() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> getReservations(
            final Long memberId,
            final Long themeId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    ) {
        final List<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                memberId,
                themeId,
                dateFrom,
                dateTo);
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<MyReservationResponse> getMyReservations(final LoginMember loginMember) {
        final List<MyReservationResponse> reservations = reservationRepository.findAllByMemberIdOrderByDateDesc(
                        loginMember.id()).stream()
                .map(MyReservationResponse::new)
                .toList();
        final List<MyReservationResponse> waitings = waitingRepository.findWaitingWithRankByMemberId(
                        loginMember.id()).stream()
                .map(MyReservationResponse::new)
                .toList();
        final ArrayList<MyReservationResponse> responses = new ArrayList<>(reservations);
        responses.addAll(waitings);
        return responses;
    }
}
