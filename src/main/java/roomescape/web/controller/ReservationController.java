package roomescape.web.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.login.LoginMember;
import roomescape.dto.reservation.MemberReservationResponse;
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationRequest;
import roomescape.service.ReservationService;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservationByAdmin(
            @RequestBody ReservationRequest reservationRequest) {
        Long savedId = reservationService.addReservation(reservationRequest);
        ReservationResponse reservationResponse = reservationService.getReservation(savedId);
        return ResponseEntity.created(URI.create("/reservations/" + savedId)).body(reservationResponse);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservationByUser(
            @RequestBody UserReservationRequest userReservationRequest,
            LoginMember loginMember) {
        ReservationRequest reservationRequest = ReservationRequest.from(userReservationRequest, loginMember.id());
        Long savedId = reservationService.addReservation(reservationRequest);
        ReservationResponse reservationResponse = reservationService.getReservation(savedId);
        return ResponseEntity.created(URI.create("/reservations/" + savedId)).body(reservationResponse);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getAllReservations(ReservationFilter reservationFilter) {
        if (reservationFilter.existFilter()) {
            List<ReservationResponse> reservationResponses = reservationService.getReservationsByFilter(
                    reservationFilter);
            return ResponseEntity.ok(reservationResponses);
        }
        List<ReservationResponse> reservationResponses = reservationService.getAllReservations();
        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> getReservationsMine(LoginMember loginMember) {
        List<MemberReservationResponse> memberReservationResponses = reservationService.getReservationByMemberId(
                loginMember.id());

        return ResponseEntity.ok(memberReservationResponses);
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable Long id) {
        ReservationResponse reservationResponse = reservationService.getReservation(id);
        return ResponseEntity.ok(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
