package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.AuthMember;
import roomescape.global.auth.LoginMember;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateReservationWithMemberRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final CreateReservationRequest request,
            @AuthMember final LoginMember member
    ) {
        final CreateReservationWithMemberRequest newRequest = new CreateReservationWithMemberRequest(
                request.date(), request.timeId(), request.themeId(), member.id());

        final ReservationResponse response = reservationService.createReservation(newRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> createByAdmin(
            @RequestBody @Valid final CreateReservationWithMemberRequest request) {
        final ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll(
            @RequestParam(value = "memberId", required = false) final Long memberId,
            @RequestParam(value = "themeId", required = false) final Long themeId,
            @RequestParam(value = "dateFrom", required = false) final LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) final LocalDate dateTo
    ) {
        final List<ReservationResponse> responses = reservationService.getReservations(
                memberId,
                themeId,
                dateFrom,
                dateTo
        );
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationService.cancelReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
