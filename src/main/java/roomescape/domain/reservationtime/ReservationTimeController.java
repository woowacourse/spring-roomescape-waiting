package roomescape.domain.reservationtime;

import jakarta.servlet.http.HttpServletRequest;
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
import roomescape.domain.reservationtime.dto.TimeCreationRequest;
import roomescape.domain.reservationtime.dto.TimeCreationResponse;
import roomescape.domain.reservationtime.dto.ReservationTimeAvailabilityResponse;
import roomescape.domain.reservationtime.dto.ReservationTimeResponse;

@RestController
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeAvailabilityResponse>> getReservationTimeAvailability(
        @RequestParam Long themeId,
        @RequestParam Long dateId
    ) {
        List<ReservationTimeAvailabilityResponse> response = reservationTimeService
            .getReservationTimeAvailability(themeId, dateId);
        return ResponseEntity.ok(response);
    }
}
