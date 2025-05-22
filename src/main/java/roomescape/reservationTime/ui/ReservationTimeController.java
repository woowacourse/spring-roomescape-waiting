package roomescape.reservationTime.ui;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationTime.application.ReservationTimeService;
import roomescape.reservationTime.application.dto.AvailableTimeRequest;
import roomescape.reservationTime.application.dto.AvailableTimeResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/times")
public class ReservationTimeController {
    private final ReservationTimeService timeService;

    @GetMapping("/available")
    public ResponseEntity<List<AvailableTimeResponse>> get(
            @Valid @ModelAttribute AvailableTimeRequest request
    ) {
        return ResponseEntity.ok(timeService.getAvailableTimes(request));
    }
}
