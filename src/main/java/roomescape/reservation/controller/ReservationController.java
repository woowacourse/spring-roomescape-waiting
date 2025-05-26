package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.infrastructure.methodargument.AuthorizedMember;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationServiceFacade;

@RestController
@AllArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationServiceFacade reservationService;

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

    @PostMapping("/waitings")
    public ResponseEntity<ReservationResponse> createWaiting(
        @RequestBody @Valid ReservationCreateRequest reservationCreateRequest,
        @AuthorizedMember MemberPrincipal memberPrincipal
    ) {
        ReservationResponse response = reservationService.createWaiting(
            reservationCreateRequest,
            memberPrincipal
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll() {
        List<ReservationResponse> responses = reservationService.findAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyReservationResponse>> findMyReservation(
        @AuthorizedMember MemberPrincipal memberPrincipal
    ) {
        List<MyReservationResponse> responses = reservationService.findMine(memberPrincipal);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}/waitings")
    public ResponseEntity<Void> deleteWaiting(
        @PathVariable Long id,
        @AuthorizedMember MemberPrincipal memberPrincipal
    ) {
        reservationService.deleteWaiting(id, memberPrincipal);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
