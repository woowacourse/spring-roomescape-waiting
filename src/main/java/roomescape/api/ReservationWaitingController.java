package roomescape.api;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.dto.ReservationWaitingResponses;
import roomescape.facade.ReservationFacade;
import roomescape.service.ReservationWaitingService;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationFacade reservationFacade;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationFacade reservationFacade,
                                        ReservationWaitingService reservationWaitingService) {
        this.reservationFacade = reservationFacade;
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> add(@RequestBody @Valid ReservationWaitingRequest request) {
        ReservationWaiting reservationWaiting = reservationFacade.addWaiting(request);
        ReservationWaitingResponse response = ReservationWaitingResponse.from(reservationWaiting);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ReservationWaitingResponses> searchMine(@RequestParam String name) {
        List<ReservationWaiting> myReservationWaitings = reservationWaitingService.getMyReservationWaitings(name);
        return ResponseEntity.ok().body(ReservationWaitingResponses.from(myReservationWaitings));
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestParam String name) {
        reservationWaitingService.cancelMyReservationWaiting(id, name);

        return ResponseEntity.noContent().build();
    }
}
