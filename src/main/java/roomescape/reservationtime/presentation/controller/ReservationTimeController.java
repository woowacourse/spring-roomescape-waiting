package roomescape.reservationtime.presentation.controller;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationtime.application.service.ReservationTimeQueryService;
import roomescape.reservationtime.presentation.dto.AvailableReservationTimeResponse;

@RequiredArgsConstructor
@Validated
@RequestMapping("/times")
@RestController
public class ReservationTimeController {

    private final ReservationTimeQueryService timeQueryService;

    @GetMapping
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableTimes(
            @Positive(message = "테마ID는 양수여야 합니다.")
            @RequestParam Long themeId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(
                timeQueryService.findAvailableTimes(themeId, date).stream()
                        .map(AvailableReservationTimeResponse::from)
                        .toList()
        );
    }
}
