package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.global.auth.AuthMember;
import roomescape.global.auth.LoginMember;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateUserReservationRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createForUser(
            @RequestBody @Valid final CreateUserReservationRequest request,
            @AuthMember final LoginMember member
    ) {
        final CreateReservationRequest newRequest = new CreateReservationRequest(
                request.date(), request.timeId(), request.themeId(), member.id()
        );
        final ReservationResponse response = reservationService.createReservation(newRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> createByAdmin(@RequestBody @Valid final CreateReservationRequest request) {
        final ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll() {
        final List<ReservationResponse> responses = reservationService.getAllReservations();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> search(
            @RequestParam(value = "memberId") final Long memberId,
            @RequestParam(value = "themeId") final Long themeId,
            @RequestParam(value = "dateFrom") final LocalDate dateFrom,
            @RequestParam(value = "dateTo") final LocalDate dateTo
    ) {
        final List<ReservationResponse> responses = reservationService.getFilteredReservations(
                memberId,
                themeId,
                dateFrom,
                dateTo
        );
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/me/reservations")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(@AuthMember final LoginMember loginMember) {
        final List<MyReservationResponse> responses = reservationService.getMyReservations(loginMember);
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationService.cancelReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
