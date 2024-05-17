package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/times")
@RestController
public class TimeController {

    ReservationTimeService reservationTimeService;

    public TimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<ReservationTimeResponse>> getTimes() {
        List<ReservationTimeResponse> times = reservationTimeService.getAllReservationTimes();
        MultipleResponse<ReservationTimeResponse> response = new MultipleResponse<>(times);

        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping("/available") // todo: rest 한 api 이름으로 바꾸기
    public ResponseEntity<MultipleResponse<AvailableReservationTimeResponse>> getReservationTimeBookedStatus(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        List<AvailableReservationTimeResponse> times
                = reservationTimeService.getReservationTimeBookedStatus(date, themeId);
        MultipleResponse<AvailableReservationTimeResponse> response = new MultipleResponse<>(times);

        return ResponseEntity.ok()
                .body(response);
    }
}
