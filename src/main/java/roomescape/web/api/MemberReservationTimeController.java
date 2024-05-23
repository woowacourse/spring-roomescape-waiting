package roomescape.web.api;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.response.time.AvailableReservationTimeResponse;
import roomescape.service.dto.response.time.ReservationTimeResponse;

@RestController
@RequiredArgsConstructor
public class MemberReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeResponse>> findAllTimes() {
        List<ReservationTimeResponse> response = reservationTimeService.findAllReservationTime();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/times/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAllAvailableTimes(
            @RequestParam(name = "date") LocalDate date,
            @RequestParam(name = "themeId") Long themeId
    ) {
        List<AvailableReservationTimeResponse> response = reservationTimeService.findAllAvailableReservationTime(
                date, themeId);
        return ResponseEntity.ok().body(response);
    }
}
