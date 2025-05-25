package roomescape.controller.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.annotation.AdminMember;
import roomescape.controller.annotation.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MemberReservationCreateRequestDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.command.ReservationWaitingCommandService;
import roomescape.service.dto.ReservationCreateDto;
import roomescape.service.query.ReservationQueryService;

@RestController
@RequestMapping("/reservations/waiting")
public class ReservationWaitingController {

    private final ReservationWaitingCommandService reservationWaitingCommandService;
    private final ReservationQueryService reservationQueryService;

    public ReservationWaitingController(ReservationWaitingCommandService reservationWaitingCommandService,
                                        ReservationQueryService reservationQueryService) {
        this.reservationWaitingCommandService = reservationWaitingCommandService;
        this.reservationQueryService = reservationQueryService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseDto> getReservationWaitings(
            @AdminMember LoginInfo loginInfo
    ) {
        return reservationQueryService.findAllReservationWaitings();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDto addReservationWaiting(
            @CurrentMember LoginInfo loginInfo,
            @RequestBody MemberReservationCreateRequestDto request
    ) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(
                request.date(), request.timeId(), request.themeId(), loginInfo.id());
        return reservationWaitingCommandService.createReservationWaiting(reservationCreateDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWaitingReservation(
            @CurrentMember LoginInfo loginInfo,
            @PathVariable("id") final Long id
    ) {
        reservationWaitingCommandService.deleteReservationWaiting(id, loginInfo);
    }
}
