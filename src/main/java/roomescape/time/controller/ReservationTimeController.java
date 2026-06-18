package roomescape.time.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.controller.dto.response.TimeOfSlotDetailDto;
import roomescape.time.service.ReservationTimeService;

import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping("/slots/times")
    public ResponseEntity<List<TimeOfSlotDetailDto>> readAvailableTimesOfSlot(
            @RequestParam("dateId") Long dateId, @RequestParam("themeId") Long themeId
    ) {
        List<TimeOfSlotDetailDto> responseData = reservationTimeService.readAvailableSlotTimes(dateId, themeId).stream()
                .map(TimeOfSlotDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

}
