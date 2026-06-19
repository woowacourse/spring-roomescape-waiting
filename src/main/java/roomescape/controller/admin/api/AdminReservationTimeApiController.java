package roomescape.controller.admin.api;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.ReservationTimeService;
import roomescape.application.service.result.ReservationTimeResult;
import roomescape.controller.admin.api.dto.request.AdminReservationTimeRequest;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;
import roomescape.controller.admin.api.query.AdminReservationTimeQuery;

@RestController
@RequestMapping("/api/admin/times")
@Validated
@RequiredArgsConstructor
public class AdminReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;
    private final AdminReservationTimeQuery reservationTimeQuery;

    @PostMapping
    public ResponseEntity<AdminReservationTimeResponse> register(
            @Valid @RequestBody AdminReservationTimeRequest request
    ) {
        ReservationTimeResult result = reservationTimeService.register(request.toCommand());
        return ResponseEntity.status(CREATED).body(AdminReservationTimeResponse.from(result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable
            @Positive(message = "예약 시간 비활성화 식별자는 양수여야 합니다.") Long id
    ) {
        reservationTimeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable
            @Positive(message = "예약 시간 활성화 식별자는 양수여야 합니다.") Long id
    ) {
        reservationTimeService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AdminReservationTimeResponse>> getAllTimes() {
        return ResponseEntity.ok(reservationTimeQuery.getAllReservationTimes());
    }
}
