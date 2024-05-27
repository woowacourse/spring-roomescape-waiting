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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.core.AuthenticationPrincipal;
import roomescape.auth.domain.AuthInfo;
import roomescape.reservation.dto.request.CreateMyReservationRequest;
import roomescape.reservation.dto.response.CreateReservationResponse;
import roomescape.reservation.dto.response.FindAvailableTimesResponse;
import roomescape.reservation.dto.response.FindReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<CreateReservationResponse> createReservation(
            @AuthenticationPrincipal AuthInfo authInfo,
            @Valid @RequestBody CreateMyReservationRequest createReservationRequest) {
        CreateReservationResponse createReservationResponse =
                reservationService.createMyReservation(authInfo, createReservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + createReservationResponse.id()))
                .body(createReservationResponse);
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<FindReservationResponse> getReservation(@PathVariable final Long id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    @GetMapping("/reservations/times")
    public ResponseEntity<List<FindAvailableTimesResponse>> getAvailableTimes(@RequestParam LocalDate date,
                                                                              @RequestParam Long themeId) {
        return ResponseEntity.ok(reservationService.getAvailableTimes(date, themeId));
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<FindReservationResponse>> searchBy(@RequestParam(required = false) Long themeId,
                                                                  @RequestParam(required = false) Long memberId,
                                                                  @RequestParam(required = false) LocalDate dateFrom,
                                                                  @RequestParam(required = false) LocalDate dateTo) {
        return ResponseEntity.ok(reservationService.searchBy(themeId, memberId, dateFrom, dateTo));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@AuthenticationPrincipal AuthInfo authInfo,
                                                  @PathVariable Long id) {
        reservationService.deleteReservation(authInfo, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/members/reservations")
    public ResponseEntity<List<FindReservationResponse>> getReservations(
            @AuthenticationPrincipal AuthInfo authInfo) {
        return ResponseEntity.ok(reservationService.getReservations(authInfo));
    }
}
