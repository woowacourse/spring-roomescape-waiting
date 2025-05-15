package roomescape.controller.rest;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationTimeService;
import roomescape.service.request.AvailableReservationTimeRequest;
import roomescape.service.request.CreateReservationTimeRequest;
import roomescape.service.response.AvailableReservationTimeResponse;
import roomescape.service.response.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createTime(
            @RequestBody @Valid final CreateReservationTimeRequest request
    ) {
        ReservationTimeResponse response = reservationTimeService.createReservationTime(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getTimes() {
        List<ReservationTimeResponse> responses = reservationTimeService.findAllReservationTimes();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable final Long id) {
        reservationTimeService.deleteReservationTimeById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getAvailableReservationTimes(
            @ModelAttribute @Valid final AvailableReservationTimeRequest request
    ) {
        List<AvailableReservationTimeResponse> responses =
                reservationTimeService.findAvailableReservationTimes(request);
        return ResponseEntity.ok(responses);
    }
}
