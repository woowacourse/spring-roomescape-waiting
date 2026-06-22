package roomescape.presentation.controller;

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
import roomescape.application.ReservationWaitingApplicationService;
import roomescape.domain.projection.ReservationWaitingWithOrder;
import roomescape.presentation.dto.ReservationWaitingRequest;
import roomescape.presentation.dto.ReservationWaitingResponse;
import roomescape.presentation.dto.ReservationWaitingResponses;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationWaitingApplicationService reservationWaitingApplicationService;

    public ReservationWaitingController(
            ReservationWaitingApplicationService reservationWaitingApplicationService
    ) {
        this.reservationWaitingApplicationService = reservationWaitingApplicationService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> add(
            @RequestBody @Valid ReservationWaitingRequest request
    ) {
        ReservationWaitingWithOrder waitingWithOrder = reservationWaitingApplicationService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationWaitingResponse.from(waitingWithOrder));
    }

    @GetMapping("/me")
    public ResponseEntity<ReservationWaitingResponses> searchMine(
            @RequestParam String name
    ) {
        List<ReservationWaitingWithOrder> waitings = reservationWaitingApplicationService.findMine(name);

        return ResponseEntity.ok()
                .body(ReservationWaitingResponses.from(waitings));
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        reservationWaitingApplicationService.deleteMine(id, name);

        return ResponseEntity.noContent().build();
    }
}
