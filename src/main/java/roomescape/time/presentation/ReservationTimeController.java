package roomescape.time.presentation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.AvailableReservationTimeInfo;
import roomescape.time.presentation.dto.AvailableReservationTimeRequest;
import roomescape.time.presentation.dto.AvailableReservationTimeResponse;
import roomescape.time.presentation.dto.ReservationTimeResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService service;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTimeResponse> responses = service.getReservationTimes()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available")
    public ResponseEntity<AvailableReservationTimeResponse> getAvailableReservationTime(@Valid @ModelAttribute AvailableReservationTimeRequest request) {
        AvailableReservationTimeInfo reservationTimeInfo = service.getAvailableReservationTime(
                request.toCommand());
        return ResponseEntity.ok(AvailableReservationTimeResponse.from(reservationTimeInfo));
    }
}
