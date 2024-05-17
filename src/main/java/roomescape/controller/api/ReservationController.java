package roomescape.controller.api;

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
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.ReservationWaitingRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.security.Accessor;
import roomescape.security.Auth;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservationsByConditions(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ReservationResponse> reservationResponses = reservationService
                .getReservationsByConditions(memberId, themeId, dateFrom, dateTo);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservationWithRanks(@Auth Accessor accessor) {
        List<MyReservationResponse> reservationResponses = reservationService
                .getMyReservationWithRanks(accessor.id());

        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid ReservationRequest request,
            @Auth Accessor accessor
    ) {
        ReservationResponse reservationResponse = reservationService.addReservation(
                request.date(),
                request.timeId(),
                request.themeId(),
                accessor.id()
        );

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationById(@PathVariable Long id) {
        reservationService.deleteReservationById(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> addReservationWaiting(
            @RequestBody @Valid ReservationWaitingRequest request,
            @Auth Accessor accessor
    ) {
        ReservationResponse response = reservationService.addReservationWaiting(
                request.date(),
                request.timeId(),
                request.themeId(),
                accessor.id()
        );

        return ResponseEntity.created(URI.create("/reservations/" + response.id() + "/waiting"))
                .body(response);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(
            @PathVariable Long id,
            @Auth Accessor accessor
    ) {
        reservationService.deleteReservationWaitingById(id, accessor.id());

        return ResponseEntity.noContent().build();
    }
}
