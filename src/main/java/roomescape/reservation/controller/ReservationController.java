package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.infrastructure.methodargument.AuthorizedMember;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.MyReservationAndWaitingResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationServiceFacade;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationServiceFacade reservationService;

    public ReservationController(ReservationServiceFacade reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid ReservationCreateRequest reservationCreateRequest,
            @AuthorizedMember MemberPrincipal memberPrincipal
    ) {
        ReservationResponse response = reservationService.createReservation(
                reservationCreateRequest,
                memberPrincipal
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> responses = reservationService.findAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationAndWaitingResponse>> getMyReservations(
            @AuthorizedMember MemberPrincipal memberPrincipal
    ) {
        List<MyReservationAndWaitingResponse> responses = reservationService.findAllMyReservationAndWaiting(memberPrincipal);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
