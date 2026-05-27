package roomescape.reservationtime.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.reservationtime.service.ReservationTimeService;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/admin/times")
public class ReservationTimeAdminController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeAdminController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @Valid @RequestBody ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.create(request.startAt());

        return ResponseEntity.status(CREATED)
                .body(ReservationTimeResponse.from(reservationTime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Positive(message = "예약 시간 id는 1 이상의 숫자여야 합니다.") @PathVariable Long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
