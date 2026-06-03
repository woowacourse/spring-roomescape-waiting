package roomescape.waiting.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Authorized;
import roomescape.auth.OwnerOnly;
import roomescape.waiting.controller.dto.ReservationWaitingRequest;
import roomescape.waiting.controller.dto.ReservationWaitingResponse;
import roomescape.waiting.service.ReservationWaitingService;
import roomescape.waiting.service.dto.ReservationWaitingResult;

@RestController
@RequestMapping("/reservations-waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(
            @RequestBody @Valid ReservationWaitingRequest request
    ) {
        ReservationWaitingResult reservationWaiting = reservationWaitingService.save(request.toCommand());
        ReservationWaitingResponse response = ReservationWaitingResponse.from(reservationWaiting);

        return ResponseEntity
                .created(URI.create("/reservations-waitings/" + response.id()))
                .body(response);
    }

    @Authorized
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteMyReservationWaiting(@OwnerOnly String userName, @PathVariable Long id) {
        reservationWaitingService.deleteById(id, userName);
    }
}

