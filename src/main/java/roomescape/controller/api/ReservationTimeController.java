package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAllReservationTimes() {
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeService.getAllReservationTimes();

        return ResponseEntity.ok(reservationTimeResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addReservationTime(
            @RequestBody @Valid ReservationTimeRequest reservationTimeRequest
    ) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.addReservationTime(
                reservationTimeRequest);

        return ResponseEntity.created(URI.create("/times/" + reservationTimeResponse.id()))
                .body(reservationTimeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTimeById(@PathVariable Long id) {
        reservationTimeService.deleteReservationTimeById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getAvailableReservationTimes(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        List<AvailableReservationTimeResponse> availableReservationTimeResponses = reservationTimeService
                .getAvailableReservationTimes(date, themeId);

        return ResponseEntity.ok(availableReservationTimeResponses);
    }
}
