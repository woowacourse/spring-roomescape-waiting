package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
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
import roomescape.reservation.dto.ReservationAdminCreateRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.request.ReservationMemberCreateRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.request.ReservationSearchRequest;
import roomescape.reservation.service.ReservationAndWaitingQueryService;
import roomescape.reservation.service.ReservationService;

@RestController
public class ReservationApiController {

    private final ReservationService reservationService;
    private final ReservationAndWaitingQueryService reservationAndWaitingQueryService;

    public ReservationApiController(ReservationService reservationService, ReservationAndWaitingQueryService reservationAndWaitingQueryService) {
        this.reservationService = reservationService;
        this.reservationAndWaitingQueryService = reservationAndWaitingQueryService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody final ReservationMemberCreateRequest request,
            @Login final LoginMemberInToken loginMember
    ) {
        Reservation reservation = reservationService.save(request, loginMember);
        ReservationResponse reservationResponse = new ReservationResponse(reservation);

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservationResponse);
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> createReservationByAdmin(@Valid @RequestBody final ReservationAdminCreateRequest request) {
        Reservation reservation = reservationService.saveByAdmin(request);
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
        final Long memberId = loginMember.id();

        return ResponseEntity.ok(reservationAndWaitingQueryService.findAllByMemberId(memberId));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, @Login LoginMemberInToken loginMember) {
        reservationService.delete(id, loginMember);

        return ResponseEntity.noContent().build();
    }
}
