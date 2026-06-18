package roomescape.domain.reservation.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.service.ReservationService;

@RestController
@Validated
public class MineReservationController {

    private final ReservationService reservationService;

    public MineReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationResponseDto>> getMineReservations(
        @RequestParam
        @NotBlank(message = "예약자명은 필수입니다.")
        @Size(max = 20, message = "예약자명의 길이는 1이상 20이하 입니다.")
        String name
    ) {
        return ResponseEntity.ok(reservationService.getMineReservations(name));
    }
}
