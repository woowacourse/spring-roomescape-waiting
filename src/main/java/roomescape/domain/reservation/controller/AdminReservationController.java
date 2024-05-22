package roomescape.domain.reservation.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.login.controller.MemberResolver;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.dto.request.ReservationAddRequest;
import roomescape.domain.reservation.dto.request.ReservationFindRequest;
import roomescape.domain.reservation.dto.response.ReservationResponse;
import roomescape.domain.reservation.dto.response.WaitingReservationResponse;
import roomescape.domain.reservation.service.ReservationService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody ReservationAddRequest reservationAddRequest,
                                                              @MemberResolver Member member) {
        reservationAddRequest = reservationAddRequest.addMemberId(member.getId());
        Reservation reservation = reservationService.addReservation(reservationAddRequest);
        ReservationResponse reservationResponse = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("/reservation/" + reservation.getId())).body(reservationResponse);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> getConditionalReservationList(
            @ModelAttribute ReservationFindRequest reservationFindRequest) {
        List<Reservation> reservations = reservationService.findFilteredReservationList(
                reservationFindRequest.themeId(), reservationFindRequest.memberId(), reservationFindRequest.dateFrom(),
                reservationFindRequest.dateTo());
        List<ReservationResponse> reservationResponses = ReservationResponse.fromList(reservations);
        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/waiting")
    public ResponseEntity<List<WaitingReservationResponse>> getWaitingReservationList() {
        List<Reservation> reservations = reservationService.findWaitingReservations();
        List<WaitingReservationResponse> waitingReservationResponses = WaitingReservationResponse.fromList(
                reservations);
        return ResponseEntity.ok(waitingReservationResponses);
    }
}
