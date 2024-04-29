package roomescape.controller.api;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.CreateTimeRequest;
import roomescape.controller.dto.CreateTimeResponse;
import roomescape.controller.dto.FindTimeResponse;
import roomescape.domain.reservation.ReservationTime;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/admin/times")
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<CreateTimeResponse> save(@RequestBody CreateTimeRequest request) {
        ReservationTime newReservationTime = reservationTimeService.save(request.startAt());
        Long id = newReservationTime.getId();

        return ResponseEntity.created(URI.create("/times/" + id))
            .body(new CreateTimeResponse(id, newReservationTime.getStartAt()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationTimeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FindTimeResponse>> findAll() {
        List<FindTimeResponse> response = reservationTimeService.findAll()
            .stream()
            .map(FindTimeResponse::from)
            .toList();

        return ResponseEntity.ok(response);
    }
}
