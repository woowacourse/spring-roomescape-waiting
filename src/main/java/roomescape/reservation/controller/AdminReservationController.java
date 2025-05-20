package roomescape.reservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RequestMapping("/admin/reservations")
@RestController
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;

    /**
     TODO 4단계
     ### 예약 대기 취소(어드민)
     ### 예약 대기 승인(어드민)
     - 예약 상태인 예약을 취소하면 자동으로 1번 대기 요청이 승인됩니다.
     */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse saveReservation(@Valid @RequestBody final AdminReservationRequest request) {
        return reservationService.saveAdminReservation(request);
    }
}
