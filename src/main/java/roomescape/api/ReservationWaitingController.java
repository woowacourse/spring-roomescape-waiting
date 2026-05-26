package roomescape.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.facade.ReservationFacade;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationFacade reservationFacade;

    public ReservationWaitingController(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> add(@RequestBody @Valid ReservationWaitingRequest request) {
        ReservationWaiting reservationWaiting = reservationFacade.addWaiting(request);
        ReservationWaitingResponse response = ReservationWaitingResponse.from(reservationWaiting);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
