package roomescape.presentation.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.ReservationTicketService;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationTicketRegisterDto;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class ReservationTicketAdminController {

    private final ReservationTicketService reservationTicketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addReservation(@RequestBody @Valid ReservationTicketRegisterDto reservationAdminRegisterDto,
                               LoginMember loginMember) {
        reservationTicketService.saveReservation(reservationAdminRegisterDto, loginMember);
    }
}
