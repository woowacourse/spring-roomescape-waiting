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
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationRequest;
import roomescape.dto.reservation.UserReservationResponse;
import roomescape.service.ReservationQueryService;
import roomescape.service.ReservationService;

@RestController
class ReservationController {

    private final ReservationService reservationService;
    private final ReservationQueryService reservationQueryService;

    public ReservationController(
            ReservationService reservationService,
            ReservationQueryService reservationQueryService
    ) {
        this.reservationService = reservationService;
        this.reservationQueryService = reservationQueryService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservationByAdmin(
            @RequestBody ReservationRequest reservationRequest) {
        Long savedId = reservationService.addReservation(reservationRequest);
        ReservationResponse reservationResponse = reservationQueryService.getReservation(savedId);
        return ResponseEntity.created(URI.create("/reservations/" + savedId)).body(reservationResponse);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservationByUser(
            @RequestBody UserReservationRequest userReservationRequest,
            LoginMember loginMember) {
        ReservationRequest reservationRequest = ReservationRequest.from(userReservationRequest, loginMember.id());
        Long savedId = reservationService.addReservation(reservationRequest);
        ReservationResponse reservationResponse = reservationQueryService.getReservation(savedId);
        return ResponseEntity.created(URI.create("/reservations/" + savedId)).body(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getAllReservations(ReservationFilter reservationFilter) {
        if (reservationFilter.existFilter()) {
            List<ReservationResponse> reservationResponses =
                    reservationQueryService.getReservationsByFilter(reservationFilter);
            return ResponseEntity.ok(reservationResponses);
        }

        List<ReservationResponse> reservationResponses = reservationQueryService.getAllReservations();
        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<UserReservationResponse>> getReservationsMine(LoginMember loginMember) {
        List<UserReservationResponse> userReservationResponses =
                reservationQueryService.getReservationByMemberId(loginMember.id());

        return ResponseEntity.ok(userReservationResponses);
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable Long id) {
        ReservationResponse reservationResponse = reservationQueryService.getReservation(id);
        return ResponseEntity.ok(reservationResponse);
    }
}
