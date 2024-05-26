package roomescape.domain.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.login.controller.MemberResolver;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.Reservation;
import roomescape.domain.reservation.domain.Status;
import roomescape.domain.reservation.dto.ReservationAddRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody ReservationAddRequest reservationAddRequest,
                                                              @MemberResolver Member member) {
        reservationAddRequest = new ReservationAddRequest(reservationAddRequest.date(), reservationAddRequest.timeId(),
                reservationAddRequest.themeId(), member.getId());
        Reservation reservation = reservationService.addReservation(reservationAddRequest);
        ReservationResponse reservationResponse = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("/reservation/" + reservation.getId())).body(reservationResponse);
    }

    @DeleteMapping("/admin/reservations/{id}")
    public ResponseEntity<Void> removeReservation(@PathVariable("id") Long id) {
        reservationService.removeReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/reservations/all")
    public ResponseEntity<List<ReservationResponse>> getAllReservationList() {
        List<Reservation> reservations = reservationService.findAllReservation();
        List<ReservationResponse> reservationResponses = ReservationResponse.fromList(reservations);
        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/admin/reservations/wait")
    public ResponseEntity<List<ReservationResponse>> getReservationWaitList() {
        List<Reservation> reservations = reservationService.findReservationByStatus(Status.RESERVATION_WAIT);
        List<ReservationResponse> reservationResponses = ReservationResponse.fromList(reservations);
        return ResponseEntity.ok(reservationResponses);
    }

    @PatchMapping("/admin/reservations/wait/{id}")
    public ResponseEntity<ReservationResponse> approveReservation(@PathVariable("id") Long id) {
        Reservation reservation = reservationService.updateReservationStatus(id, Status.RESERVATION);
        ReservationResponse reservationResponse = ReservationResponse.from(reservation);
        return ResponseEntity.ok(reservationResponse);
    }
}
