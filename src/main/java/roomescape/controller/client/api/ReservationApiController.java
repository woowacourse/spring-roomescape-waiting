package roomescape.controller.client.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.ReservationService;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.controller.client.api.dto.condition.ReservationSearchCondition;
import roomescape.controller.client.api.dto.request.ReservationChangeRequest;
import roomescape.controller.client.api.dto.request.ReservationRequest;
import roomescape.controller.client.api.dto.response.ReservationDetailResponse;
import roomescape.controller.client.api.dto.response.ReservationSearchResponse;
import roomescape.controller.client.api.dto.response.ReservationSlotResponse;
import roomescape.controller.client.api.query.ReservationQuery;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
@Validated
public class ReservationApiController {

    private final ReservationService reservationService;
    private final ReservationQuery reservationQuery;

    @PostMapping
    public ResponseEntity<ReservationSlotResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        ReservationSlotResult result = reservationService.reserve(request.toCommand());
        return ResponseEntity.created(URI.create("/api/reservations/" + result.reservation().id()))
                .body(ReservationSlotResponse.from(result));
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationSlotResponse> joinWaitingList(@Valid @RequestBody ReservationRequest request) {
        ReservationSlotResult result = reservationService.addWaiting(request.toCommand());
        return ResponseEntity.created(URI.create("/api/reservations/" + result.reservation().id()))
                .body(ReservationSlotResponse.from(result));
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<ReservationSlotResponse> changeReservation(
            @PathVariable @Positive(message = "예약 식별자는 양수입니다.") Long reservationId,
            @Valid @RequestBody ReservationChangeRequest request
    ) {
        ReservationSlotResult result = reservationService.change(reservationId, request.toCommand());
        return ResponseEntity.ok(ReservationSlotResponse.from(result));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancel(
            @PathVariable @Positive(message = "예약 식별자는 양수입니다.") Long reservationId
    ) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailResponse> getReservation(
            @PathVariable @Positive(message = "예약 식별자는 양수입니다.") Long reservationId
    ) {
        return ResponseEntity.ok(reservationQuery.findByReservationId(reservationId));
    }

    @GetMapping
    public ResponseEntity<Page<ReservationSearchResponse>> searchBy(
            @ModelAttribute @Valid ReservationSearchCondition condition,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reservationQuery.search(condition, pageable));
    }
}
