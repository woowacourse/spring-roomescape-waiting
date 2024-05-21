package roomescape.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.request.ReservationTimeSaveRequest;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponses;

import java.net.URI;

@RestController
@RequestMapping("/admin/times")
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<ReservationTimeResponses> getTimes() {
        ReservationTimeResponses reservationTimeResponses = reservationTimeService.getTimes();
        return ResponseEntity.ok(reservationTimeResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> saveTime(@RequestBody @Valid ReservationTimeSaveRequest reservationTimeSaveRequest) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.saveTime(reservationTimeSaveRequest);
        return ResponseEntity.created(URI.create("/times/" + reservationTimeResponse.id()))
                .body(reservationTimeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable("id") Long id) {
        reservationTimeService.deleteTime(id);
        return ResponseEntity.noContent().build();
    }
}
