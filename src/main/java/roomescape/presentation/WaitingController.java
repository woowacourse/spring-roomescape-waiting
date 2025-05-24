package roomescape.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authenticated;
import roomescape.dto.request.WaitingCreateRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping(value = "/api/waiting")
public class WaitingController {
    private final ReservationService reservationService;

    public WaitingController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createNewWaiting(
            @Authenticated Long memberId,
            @Valid @RequestBody WaitingCreateRequest request) {
        WaitingResponse waitingResponse = reservationService.createWaiting(
                memberId, request.timeId(), request.themeId(), request.date(), LocalDateTime.now());
        return ResponseEntity
                .created(URI.create("/reservations/waitings/" + waitingResponse.id()))
                .body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        reservationService.deleteWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
