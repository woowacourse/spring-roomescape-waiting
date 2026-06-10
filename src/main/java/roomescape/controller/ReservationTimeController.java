package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.controller.dto.request.AvailableTimeFindRequest;
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.controller.dto.response.ReservationTimeResponses;
import roomescape.domain.reservation.ReservationTime;
import roomescape.service.ReservationTimeService;

import java.net.URI;
import java.util.List;

@RestController
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping("/admin/times")
    public ResponseEntity<ReservationTimeResponse> create(
            @Valid @RequestBody ReservationTimeCreateRequest request
    ) {
        ReservationTime time = reservationTimeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(time.getId())
                .toUri();

        return ResponseEntity.created(location).body(ReservationTimeResponse.toDto(time));
    }

    @GetMapping("/times")
    public ResponseEntity<ReservationTimeResponses> findAll() {
        List<ReservationTime> reservationTimes = reservationTimeService.findAll();
        return ResponseEntity.ok(ReservationTimeResponses.toDto(reservationTimes));
    }

    @GetMapping("/times/available")
    public ResponseEntity<ReservationTimeResponses> findAvailable(
            @Valid @ModelAttribute AvailableTimeFindRequest request
    ) {
        List<ReservationTime> reservationTimes = reservationTimeService.findAvailable(request);
        return ResponseEntity.ok(ReservationTimeResponses.toDto(reservationTimes));
    }

    @DeleteMapping("/admin/times/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
