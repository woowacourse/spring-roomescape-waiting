package roomescape.controller.api;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.FindTimeAndAvailabilityResponse;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.FindTimeAndAvailabilityDto;

@RestController
@RequestMapping("/times")
public class UserReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public UserReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<FindTimeAndAvailabilityResponse>> findAllWithAvailability(
        @RequestParam LocalDate date, @RequestParam Long id) {

        List<FindTimeAndAvailabilityDto> appResponses = reservationTimeService
            .findAllWithBookAvailability(date, id);

        List<FindTimeAndAvailabilityResponse> webResponses = appResponses.stream()
            .map(response -> new FindTimeAndAvailabilityResponse(
                response.id(),
                response.startAt(),
                response.alreadyBooked()
            )).toList();

        return ResponseEntity.ok(webResponses);
    }
}
