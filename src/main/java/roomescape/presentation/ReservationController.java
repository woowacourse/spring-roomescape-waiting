package roomescape.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authenticated;
import roomescape.dto.request.ReservationCreateRequest;
import roomescape.dto.request.WaitingCreateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWithStatusResponse;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping(value = "/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createNewReservation(
            @Authenticated Long memberId,
            @Valid @RequestBody ReservationCreateRequest request) {
        ReservationResponse reservationResponse = reservationService.createReservation(
                memberId, request.timeId(), request.themeId(), request.date()
        );
        return ResponseEntity
                .created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> createNewWaiting(
            @Authenticated Long memberId,
            @Valid @RequestBody WaitingCreateRequest request) {
        WaitingResponse waitingResponse = reservationService.createWaiting(
                memberId, request.timeId(), request.themeId(), request.date(), LocalDateTime.now()
        );
        return ResponseEntity
                .created(URI.create("/reservations/waitings/" + waitingResponse.id()))
                .body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public List<ReservationWithStatusResponse> getMyBookingHistory(@Authenticated Long id) {
        return reservationService.findBookingHistory(id);
    }
}
