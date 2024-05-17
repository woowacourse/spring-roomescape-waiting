package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.reservation.controller.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationTimeDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationTimeResponse;
import roomescape.reservation.service.ReservationTimeService;

@Controller
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> save(
            @RequestBody @Valid final ReservationTimeSaveRequest reservationTimeSaveRequest
    ) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.save(reservationTimeSaveRequest);
        return ResponseEntity.created(URI.create("/times/" + reservationTimeResponse.id()))
                .body(reservationTimeResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAll() {
        return ResponseEntity.ok(reservationTimeService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationTimeDeleteResponse> delete(@PathVariable("id") final long id) {
        return ResponseEntity.ok().body(reservationTimeService.delete(id));
    }
}
