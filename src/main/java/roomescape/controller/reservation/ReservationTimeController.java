package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.service.reservation.ReservationTimeService;
import roomescape.service.reservation.dto.AvailableTimeRequest;
import roomescape.service.reservation.dto.AvailableTimeResponse;
import roomescape.service.reservation.dto.ReservationTimeRequest;
import roomescape.service.reservation.dto.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> postReservationTime(
            @RequestBody @Valid ReservationTimeRequest reservationTimeRequest
    ) {
        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        URI location = UriComponentsBuilder.newInstance()
                .path("/times/{id}")
                .buildAndExpand(reservationTime.id())
                .toUri();

        return ResponseEntity.created(location)
                .body(reservationTime);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAllReservationTimes();
        return ResponseEntity.ok(reservationTimes);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<AvailableTimeResponse>> getAvailableTimes(
            @ModelAttribute AvailableTimeRequest availableTimeRequest
    ) {
        List<AvailableTimeResponse> availableTimes = reservationTimeService.findAvailableTimes(availableTimeRequest);
        return ResponseEntity.ok(availableTimes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent()
                .build();
    }
}
