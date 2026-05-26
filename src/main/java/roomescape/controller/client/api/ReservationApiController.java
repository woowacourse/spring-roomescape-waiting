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
import roomescape.controller.client.api.dto.ReservationChangeRequest;
import roomescape.controller.client.api.dto.ReservationDetailResponse;
import roomescape.controller.client.api.dto.ReservationRequest;
import roomescape.controller.client.api.dto.ReservationResponse;
import roomescape.query.ReservationQuery;
import roomescape.query.ReservationSearchCondition;
import roomescape.query.ReservationSearchResponse;
import roomescape.service.ReservationService;
import roomescape.service.result.ReservationResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
@Validated
public class ReservationApiController {

    private final ReservationService reservationService;
    private final ReservationQuery reservationQuery;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        ReservationResult result = reservationService.reserve(request.toCommand());
        return ResponseEntity.created(URI.create("/api/reservations/" + result.id()))
                .body(ReservationResponse.from(result));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> changeReservation(
            @PathVariable @Positive(message = "예약 변경 식별자는 양수입니다.") Long id,
            @Valid @RequestBody ReservationChangeRequest request
    ) {
        ReservationResult result = reservationService.change(id, request.toCommand());
        return ResponseEntity.ok(ReservationResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable @Positive(message = "예약 취소 식별자는 양수입니다.") Long id
    ) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDetailResponse> getReservation(@PathVariable Long id) {
        ReservationResult result = reservationService.getReservation(id);
        return ResponseEntity.ok(ReservationDetailResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<Page<ReservationSearchResponse>> searchBy(
            @ModelAttribute @Valid ReservationSearchCondition condition,
            Pageable pageable
    ) {
        return ResponseEntity.ok(reservationQuery.search(condition, pageable));
    }
}
