package roomescape.time.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.controller.dto.AvailableTimeResponse;
import roomescape.time.controller.dto.ReservationTimeResponse;
import roomescape.time.service.ReservationTimeQueryService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeQueryService reservationTimeQueryService;

    public ReservationTimeController(ReservationTimeQueryService reservationTimeQueryService) {
        this.reservationTimeQueryService = reservationTimeQueryService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> readAll() {
        List<ReservationTimeResponse> responses = reservationTimeQueryService.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping(value = "/available-times")
    public ResponseEntity<List<AvailableTimeResponse>> readAvailable(
            @RequestParam("themeId") Long themeId,
            @RequestParam("date") LocalDate date
    ) {
        List<AvailableTimeResponse> responses = reservationTimeQueryService.queryAvailableTimes(themeId, date)
                .availableTimeQueryResults()
                .stream()
                .map(AvailableTimeResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
