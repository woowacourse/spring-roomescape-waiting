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
import roomescape.controller.admin.api.dto.AdminReservationRequest;
import roomescape.controller.admin.api.dto.AdminReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.result.ReservationResult;

@RestController
@RequestMapping("/api/admin/reservations")
@Validated
@RequiredArgsConstructor
public class AdminReservationApiController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<AdminReservationResponse> reserve(@Valid @RequestBody AdminReservationRequest request) {
        ReservationResult result = reservationService.reserve(request.toCommand());
        return ResponseEntity.status(CREATED).body(AdminReservationResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable
            @Positive(message = "예약 취소 식별자는 양수여야 합니다.") Long id
    ) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AdminReservationResponse>> getAllReservations() {
        List<AdminReservationResponse> response = reservationService.getAllReservations()
                .stream()
                .map(AdminReservationResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
