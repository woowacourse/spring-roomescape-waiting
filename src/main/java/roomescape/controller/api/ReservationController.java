package roomescape.controller.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MemberReservationCreateRequestDto;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.command.ReservationCommandService;
import roomescape.service.query.ReservationQueryService;
import roomescape.service.dto.ReservationCreateDto;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;

    public ReservationController(ReservationQueryService reservationQueryService,
                                 ReservationCommandService reservationCommandService) {
        this.reservationQueryService = reservationQueryService;
        this.reservationCommandService = reservationCommandService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseDto> getAllReservations() {
        return reservationQueryService.findAllReservations();
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public List<MyReservationResponseDto> getMyReservations(
            @CurrentMember LoginInfo loginInfo
    ) {
        return reservationQueryService.findMyReservations(loginInfo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDto addReservation(
            @CurrentMember LoginInfo loginInfo,
            @RequestBody final MemberReservationCreateRequestDto requestDto
    ) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(
                requestDto.date(), requestDto.timeId(), requestDto.themeId(), loginInfo.id());
        return reservationCommandService.bookReservation(reservationCreateDto);
    }
}
