package roomescape.controller.admin.api;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.ReservationService;
import roomescape.application.service.result.ReservationSlotResult;
import roomescape.controller.admin.api.dto.request.AdminReservationRequest;
import roomescape.controller.admin.api.dto.response.AdminReservationSlotResponse;
import roomescape.controller.admin.api.query.AdminReservationQuery;

@RestController
@RequestMapping("/api/admin/reservations")
@Validated
@RequiredArgsConstructor
public class AdminReservationApiController {

    private final ReservationService reservationService;
    private final AdminReservationQuery reservationQuery;

    @PostMapping
    public ResponseEntity<AdminReservationSlotResponse> reserve(@Valid @RequestBody AdminReservationRequest request) {
        ReservationSlotResult result = reservationService.reserve(request.toCommand());
        return ResponseEntity.status(CREATED).body(AdminReservationSlotResponse.from(result));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable
            @Positive(message = "예약 식별자는 양수여야 합니다.") Long reservationId
    ) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AdminReservationSlotResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationQuery.getAllReservations());
    }
}
