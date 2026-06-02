package roomescape.date.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.date.controller.dto.response.ReservationDateDetailDto;
import roomescape.date.service.ReservationDateService;

import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ReservationDateController {

    private final ReservationDateService reservationDateService;

    @GetMapping("/dates")
    public ResponseEntity<List<ReservationDateDetailDto>> getReservationDates() {
        List<ReservationDateDetailDto> responseData = reservationDateService.readDatesAfterToday().stream()
                .map(ReservationDateDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/slot-dates")
    public ResponseEntity<List<ReservationDateDetailDto>> getSlotDates(
            @RequestParam Long themeId
    ) {
        List<ReservationDateDetailDto> responseData = reservationDateService.readSlotOfDatesByThemeId(themeId).stream()
                .map(ReservationDateDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

}
