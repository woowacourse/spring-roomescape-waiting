package roomescape.time.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.AvailableReservationTimeInfo;
import roomescape.time.presentation.dto.AvailableReservationTimeRequest;
import roomescape.time.presentation.dto.AvailableReservationTimeResponse;
import roomescape.time.presentation.dto.ReservationTimeResponse;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService service;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes(
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "페이지 번호는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "10")
            @Positive(message = "조회 개수는 양수여야 합니다.") int size
    ) {
        List<ReservationTimeResponse> responses = service.getReservationTimes(page, size)
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
