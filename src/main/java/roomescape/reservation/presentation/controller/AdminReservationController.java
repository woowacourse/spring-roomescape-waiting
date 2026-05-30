package roomescape.reservation.presentation.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.dto.ReservationPageResult;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.presentation.dto.ReservationPageResponse;

@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
@Validated
@RestController
public class AdminReservationController {

    private final ReservationQueryService reservationQueryService;

    @GetMapping
    public ResponseEntity<ReservationPageResponse> findAllByPage(
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "페이지 값은 0 이상이어야 합니다.")
            int page
    ) {
        ReservationPageResult result = reservationQueryService.findAllByPage(page);

        return ResponseEntity.ok(ReservationPageResponse.from(result));
    }
}
