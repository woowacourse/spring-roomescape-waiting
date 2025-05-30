package roomescape.presentation.controller.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.LoginMember;
import roomescape.dto.response.MemberReservationResponseDto;
import roomescape.service.ReservationService;

@RequiredArgsConstructor
@RequestMapping("/reservations-mine")
@RestController
public class MemberReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MemberReservationResponseDto> getMemberReservations(final LoginMember loginMember) {
        return reservationService.getReservationsOfMember(loginMember);
    }
}
