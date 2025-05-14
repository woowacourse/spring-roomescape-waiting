package roomescape.presentation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.LoginMember;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations-mine")
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MemberReservationResponseDto> getMemberReservations(LoginMember loginMember) {
        return reservationService.getReservationsOfMember(loginMember);
    }
}
