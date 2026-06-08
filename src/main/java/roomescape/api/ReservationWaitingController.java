package roomescape.api;

import jakarta.validation.Valid;
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
import roomescape.application.ReservationApplicationService;
import roomescape.domain.WaitingWithOrder;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.dto.ReservationWaitingResponses;
import roomescape.service.ReservationWaitingService;

import java.util.List;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationApplicationService reservationApplicationService;
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationApplicationService reservationApplicationService,
                                        ReservationWaitingService reservationWaitingService) {
        this.reservationApplicationService = reservationApplicationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> add(@RequestBody @Valid ReservationWaitingRequest request) {
        WaitingWithOrder waitingWithOrder = reservationApplicationService.addWaiting(request);
        ReservationWaitingResponse response = ReservationWaitingResponse.from(waitingWithOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ReservationWaitingResponses> searchMine(@RequestParam String name) {
        List<WaitingWithOrder> myReservationWaitings = reservationWaitingService.getMyReservationWaitings(name);
        return ResponseEntity.ok().body(ReservationWaitingResponses.from(myReservationWaitings));
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestParam String name) {
        reservationWaitingService.cancelMyReservationWaiting(id, name);

        return ResponseEntity.noContent().build();
    }
}
