package roomescape.reservation.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.config.annotation.AuthMember;
import roomescape.member.entity.Member;
import roomescape.reservation.controller.dto.request.ReservationRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    private AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
        @AuthMember Member member,
        @RequestBody @Valid ReservationRequest request
    ) {

        return ReservationResponse.from(
                reservationService.addReservation(
                        member,
                        request.date(),
                        request.themeId(),
                        request.themeId()
                )
        );
    }

}
