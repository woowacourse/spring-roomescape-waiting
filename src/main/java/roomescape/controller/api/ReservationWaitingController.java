package roomescape.controller.api;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MemberReservationCreateRequestDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.ReservationWaitingService;
import roomescape.service.dto.ReservationCreateDto;

@RestController
@RequestMapping("/reservations/waiting")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping("/waiting")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseDto> getReservationWaitings() {
        return reservationWaitingService.findAllReservationWaitings();
    }

    @PostMapping("/waiting")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDto addReservationWaiting(
            @CurrentMember LoginInfo loginInfo,
            @RequestBody MemberReservationCreateRequestDto request
    ) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(request.date(), request.timeId(),
                request.themeId(), loginInfo.id());
        return reservationWaitingService.createReservationWaiting(reservationCreateDto);
    }

    @DeleteMapping("/waiting/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWaitingReservation(
            @PathVariable("id") final Long id
    ) {
        reservationWaitingService.deleteReservationWaiting(id);
    }
}
