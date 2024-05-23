package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Login;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;

@RestController
public class ReservationApiController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationApiController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping(path = {"/reservations", "/admin/reservations"})
    public ResponseEntity<ReservationResponse> createMemberReservation(
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest,
            @Login LoginMemberInToken loginMemberInToken
    ) {
        Reservation reservation = reservationService.save(reservationCreateRequest, loginMemberInToken);
        ReservationResponse reservationResponse = new ReservationResponse(reservation);

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservationResponse);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        List<ReservationResponse> reservationResponses = reservationService.findAll();

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> findAllBySearchCond(
            @Valid ReservationSearchRequest reservationSearchRequest
    ) {
        List<ReservationResponse> reservationResponses = reservationService.findAllBySearch(reservationSearchRequest);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/me")
    public ResponseEntity<List<MyReservationResponse>> myReservations(@Login LoginMemberInToken loginMemberInToken) {
        List<MyReservationResponse> myReservationResponses = new ArrayList<>();
        final Long memberId = loginMemberInToken.id();

        myReservationResponses.addAll(reservationService.findAllByMemberId(memberId));
        myReservationResponses.addAll(waitingService.findWaitingWithRanksByMemberId(memberId));

        return ResponseEntity.ok(myReservationResponses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
