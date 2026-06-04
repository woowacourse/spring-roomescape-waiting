package roomescape.domain.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.infra.queue.JobResult;

@Validated
@RestController
public class ReservationController {

    private final ReservationQueue reservationQueue;
    private final ReservationService reservationService;

    public ReservationController(ReservationQueue reservationQueue, ReservationService reservationService) {
        this.reservationQueue = reservationQueue;
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<Map<String, String>> createReservation(
            @RequestBody @Valid ReservationRequest request
    ) {
        String jobId = reservationQueue.enqueue(request);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @GetMapping("/reservations/status/{jobId}")
    public ResponseEntity<JobResult<ReservationResponse>> getJobStatus(@PathVariable String jobId) {
        JobResult<ReservationResponse> result = reservationQueue.getResult(jobId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<TimeResponse>> getReservations(
            @RequestParam LocalDate date, @RequestParam Long themeId
    ) {
        List<TimeResponse> responses = reservationService.getReservations(date, themeId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations/mine")
    public ResponseEntity<MyReservationsResponse> getMyReservations(
            @RequestParam @NotBlank @Size(max = 100) String name
    ) {
        MyReservationsResponse response = reservationService.getMyReservations(name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id
    ) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reservations/{id}")
    public ResponseEntity<Void> updateMyReservation(
            @PathVariable Long id,
            @RequestBody ReservationFixRequest request
    ) {
        reservationService.updateMyReservation(id, request);
        return ResponseEntity.noContent().build();
    }
}
