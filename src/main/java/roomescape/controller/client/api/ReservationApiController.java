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
import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.controller.client.api.dto.request.ReservationChangeRequest;
import roomescape.controller.client.api.dto.response.ReservationDetailResponse;
import roomescape.controller.client.api.dto.request.ReservationRequest;
import roomescape.controller.client.api.dto.response.ReservationResponse;
import roomescape.controller.client.api.dto.condition.ReservationSearchCondition;
import roomescape.controller.client.api.dto.response.ReservationSearchResponse;
import roomescape.service.ReservationService;
import roomescape.service.result.ReservationResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
@Validated
public class ReservationApiController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        ReservationResult result = reservationService.reserve(request.toCommand());
        return ResponseEntity.created(URI.create("/api/reservations/entries/" + result.entry().id()))
                .body(ReservationResponse.from(result));
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> joinWaitingList(@Valid @RequestBody ReservationRequest request) {
        ReservationResult result = reservationService.addWaiting(request.toCommand());
        return ResponseEntity.created(URI.create("/api/reservations/entries/" + result.entry().id()))
                .body(ReservationResponse.from(result));
    }

    @PatchMapping("/entries/{entryId}")
    public ResponseEntity<ReservationResponse> changeReservation(
            @PathVariable @Positive(message = "예약 엔트리 식별자는 양수입니다.") Long entryId,
            @Valid @RequestBody ReservationChangeRequest request
    ) {
        ReservationResult result = reservationService.change(entryId, request.toCommand());
        return ResponseEntity.ok(ReservationResponse.from(result));
    }

    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<Void> cancel(
            @PathVariable @Positive(message = "예약 엔트리 식별자는 양수입니다.") Long entryId
    ) {
        reservationService.cancelReservation(entryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entries/{entryId}")
    public ResponseEntity<ReservationDetailResponse> getReservationEntry(
            @PathVariable @Positive(message = "예약 엔트리 식별자는 양수입니다.") Long entryId
    ) {
        ReservationResult result = reservationService.getReservationEntry(entryId);
        return ResponseEntity.ok(ReservationDetailResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<Page<ReservationSearchResponse>> searchBy(
            @ModelAttribute @Valid ReservationSearchCondition condition,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reservationService.search(condition, pageable)
                .map(ReservationSearchResponse::from));
    }
}
