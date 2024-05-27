package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.reservation.ReservationTimeRequest;
import roomescape.service.dto.reservation.ReservationTimeResponse;
import roomescape.service.dto.time.AvailableTimeRequest;
import roomescape.service.dto.time.AvailableTimeResponse;

@RestController
public class ReservationTimeRestController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeRestController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public List<ReservationTimeResponse> findReservationTimes() {
        return reservationTimeService.findAllReservationTimes();
    }

    @GetMapping("/times/available")
    public List<AvailableTimeResponse> findAvailableReservationTimes(@Valid AvailableTimeRequest request) {
        return reservationTimeService.findAvailableReservationTimes(request);
    }

    @PostMapping("/admin/times")
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @Valid @RequestBody ReservationTimeRequest request) {
        ReservationTimeResponse response = reservationTimeService.createReservationTime(request);
        URI uri = UriComponentsBuilder.fromPath("/times/{id}").build(response.getId());

        return ResponseEntity.created(uri).build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/times/{id}")
    public void deleteReservationTime(@PathVariable long id) {
        reservationTimeService.deleteReservationTime(id);
    }
}
