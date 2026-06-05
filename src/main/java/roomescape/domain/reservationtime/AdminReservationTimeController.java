package roomescape.domain.reservationtime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.AdminRequestValidator;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;
import roomescape.domain.reservationtime.dto.ReservationTimeResponse;
import roomescape.domain.reservationtime.dto.TimeCreationRequest;
import roomescape.domain.reservationtime.dto.TimeCreationResponse;

@RestController
@RequiredArgsConstructor
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping("/admin/times")
    public ResponseEntity<List<ReservationTimeResponse>> getAllReservationTime() {
        List<ReservationTimeResponse> response = reservationTimeService.getAllReservationTime();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/times")
    public ResponseEntity<TimeCreationResponse> createReservationTime(
        @Valid @RequestBody TimeCreationRequest createTimeRequest
    ) {
        TimeCreationResponse response = reservationTimeService.createReservationTime(createTimeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/admin/times/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
