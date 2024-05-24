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
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.request.ReservationSearchRequest;
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
            @Valid @RequestBody final ReservationCreateRequest request,
            @Login final LoginMemberInToken loginMember
    ) {
        Reservation reservation = reservationService.save(request, loginMember);
        ReservationResponse reservationResponse = new ReservationResponse(reservation);

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservationResponse);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        List<ReservationResponse> reservationResponses = reservationService.findAll();

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> findAllBySearchCond(@Valid ReservationSearchRequest request) {
        List<ReservationResponse> reservationResponses = reservationService.findAllBySearch(request);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/me")
    public ResponseEntity<List<MyReservationResponse>> myReservations(@Login LoginMemberInToken loginMember) {
        List<MyReservationResponse> myReservationResponses = new ArrayList<>();
        final Long memberId = loginMember.id();

        myReservationResponses.addAll(reservationService.findAllByMemberId(memberId));
        myReservationResponses.addAll(waitingService.findWaitingWithRanksByMemberId(memberId));

        return ResponseEntity.ok(myReservationResponses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, @Login LoginMemberInToken loginMember) {
        reservationService.delete(id, loginMember);

        return ResponseEntity.noContent().build();
    }
}
