package roomescape.controller.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

import java.net.URI;

@RequestMapping("/admin/times")
@RestController
public class AdminTimeController {

    private final ReservationTimeService reservationTimeService;

    public AdminTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addTime(@RequestBody ReservationTimeRequest request) {
        ReservationTimeResponse response = reservationTimeService.addReservationTime(request);
        URI location = URI.create("/times/" + response.id());

        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable("id") Long id) {
        reservationTimeService.deleteReservationTimeById(id);

        return ResponseEntity.noContent()
                .build();
    }
}
