package roomescape.api.controller;

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
import roomescape.api.dto.ReservationWaitingRequest;
import roomescape.api.dto.ReservationWaitingResponse;
import roomescape.api.dto.ReservationWaitingResponses;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.projection.ReservationWaitingWithOrder;
import roomescape.service.ReservationWaitingCommandService;
import roomescape.service.ReservationWaitingQueryService;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationWaitingCommandService reservationWaitingCommandService;
    private final ReservationWaitingQueryService reservationWaitingQueryService;

    public ReservationWaitingController(
            ReservationWaitingCommandService reservationWaitingCommandService,
            ReservationWaitingQueryService reservationWaitingQueryService
    ) {
        this.reservationWaitingCommandService = reservationWaitingCommandService;
        this.reservationWaitingQueryService = reservationWaitingQueryService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> add(
            @RequestBody @Valid ReservationWaitingRequest request
    ) {
        ReservationWaiting reservationWaiting = reservationWaitingCommandService.save(request);
        ReservationWaitingWithOrder waitingWithOrder = reservationWaitingQueryService.getWithOrderById(
                reservationWaiting.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationWaitingResponse.from(waitingWithOrder));
    }

    @GetMapping("/me")
    public ResponseEntity<ReservationWaitingResponses> searchMine(
            @RequestParam String name
    ) {
        List<ReservationWaitingWithOrder> waitings = reservationWaitingQueryService.findMine(name);

        return ResponseEntity.ok()
                .body(ReservationWaitingResponses.from(waitings));
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        reservationWaitingCommandService.deleteMine(id, name);

        return ResponseEntity.noContent().build();
    }
}
