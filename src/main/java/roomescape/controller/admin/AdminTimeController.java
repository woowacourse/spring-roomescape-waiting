package roomescape.controller.admin;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.dto.ReservationTimeRequest;
import roomescape.domain.dto.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/admin/times")
public class AdminTimeController {
    private final ReservationTimeService reservationTimeService;

    public AdminTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(@RequestBody ReservationTimeRequest reservationTimeRequest) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(reservationTimeRequest);
        return ResponseEntity.created(URI.create("/admin/times/" + reservationTimeResponse.id())).body(
                reservationTimeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
