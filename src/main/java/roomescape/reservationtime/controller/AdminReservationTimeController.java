package roomescape.reservationtime.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.reservationtime.service.ReservationTimeService;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/admin/times")
@RequiredArgsConstructor
public class AdminReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(@RequestBody @Valid ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = reservationTimeService.create(request.startAt());

        return ResponseEntity.status(CREATED)
                .body(ReservationTimeResponse.from(reservationTime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
