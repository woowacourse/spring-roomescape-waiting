package roomescape.presentation.api;

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
import roomescape.application.ReservationService;
import roomescape.application.dto.request.ReservationRequest;
import roomescape.application.dto.request.ReservationWaitingRequest;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.presentation.Auth;
import roomescape.presentation.dto.Accessor;
import roomescape.presentation.dto.request.ReservationWaitingWebRequest;
import roomescape.presentation.dto.request.ReservationWebRequest;

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
        List<MyReservationResponse> myReservationResponses = reservationService
                .getMyReservationWithRanks(accessor.id());

        return ResponseEntity.ok(myReservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid ReservationWebRequest request,
            @Auth Accessor accessor
    ) {
        ReservationRequest reservationRequest = request.toReservationRequest(accessor.id());
        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest);

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
            @RequestBody @Valid ReservationWaitingWebRequest request,
            @Auth Accessor accessor
    ) {
        ReservationWaitingRequest reservationWaitingRequest = request.toReservationWaitingRequest(accessor.id());
        ReservationResponse reservationResponse = reservationService.addReservationWaiting(reservationWaitingRequest);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id() + "/waiting"))
                .body(reservationResponse);
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
