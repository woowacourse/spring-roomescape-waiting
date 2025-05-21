package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.service.WaitingQueryService;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final WaitingQueryService waitingQueryService;

    public List<ReservationResponse> getFilteredReservations(Long themeId, Long memberId, LocalDate from,
                                                             LocalDate to) {
        if (themeId == null && memberId == null && from == null && to == null) {
            return getAllReservations();
        }

        List<Reservation> reservations = reservationRepository.findFilteredReservations(themeId, memberId, from, to);

        return ReservationResponse.from(reservations);
    }

    private List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        return ReservationResponse.from(reservations);
    }

    public List<MyReservationResponse> getAllReservations(Long memberId) {
        List<MyReservationResponse> responses = new ArrayList<>();

        responses.addAll(getReservations(memberId));
        responses.addAll(waitingQueryService.getWaitingReservations(memberId));

        return responses;
    }

    private List<MyReservationResponse> getReservations(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);

        return MyReservationResponse.from(reservations);
    }

    public boolean existsReservation(Long memberId, LocalDate date, Long timeId) {
        return reservationRepository.existsByMemberIdAndDateAndTimeId(memberId, date, timeId);
    }

    public boolean existsReservation(LocalDate date, Long timeId) {
        return reservationRepository.existsByDateAndTimeId(date, timeId);
    }
}
