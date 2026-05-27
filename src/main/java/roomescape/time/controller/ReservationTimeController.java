package roomescape.time.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.controller.dto.response.ReservationTimeDetailDto;
import roomescape.time.service.ReservationTimeService;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeDetailDto>> readAvailableTimes(
        @RequestParam("dateId") Long dateId, @RequestParam("themeId") Long themeId) {
        List<ReservationTimeDetailDto> responseData = reservationTimeService.readAvailableTimes(
                dateId, themeId).stream()
            .map(ReservationTimeDetailDto::from)
            .toList();
        return ResponseEntity.ok(responseData);
    }

}
