package roomescape.presentation.api.reservation;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationTimeService;
import roomescape.application.reservation.dto.AvailableReservationTimeResult;
import roomescape.application.reservation.dto.ReservationTimeResult;
import roomescape.presentation.api.reservation.request.CreateReservationTimeRequest;
import roomescape.presentation.api.reservation.response.AvailableReservationTimeResponse;
import roomescape.presentation.api.reservation.response.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationService;

    public ReservationTimeController(ReservationTimeService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @Valid @RequestBody CreateReservationTimeRequest createReservationTImeRequest) {
        Long id = reservationService.create(createReservationTImeRequest.toCreateParameter());
        ReservationTimeResult reservationTimeResult = reservationService.findById(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationTimeResponse.from(reservationTimeResult));
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        List<ReservationTimeResult> reservationTimeResults = reservationService.findAll();
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeResults.stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(reservationTimeResponses);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAll(@RequestParam("themeId") Long themeId,
                                                                          @RequestParam("date") LocalDate reservationDate) {
        List<AvailableReservationTimeResult> availableTimes = reservationService.findAvailableTimesByThemeIdAndDate(
                themeId,
                reservationDate
        );
        List<AvailableReservationTimeResponse> availableReservationTimeResponses = availableTimes.stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(availableReservationTimeResponses);
    }

    @DeleteMapping("/{reservationTimeId}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("reservationTimeId") Long reservationTimeId) {
        reservationService.deleteById(reservationTimeId);
        return ResponseEntity.noContent().build();
    }
}
