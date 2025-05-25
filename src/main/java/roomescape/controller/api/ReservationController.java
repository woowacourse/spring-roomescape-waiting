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
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationCreateDto;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseDto> getAllReservations() {
        return reservationService.findAllReservationResponses();
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public List<MyReservationResponseDto> getMyReservations(
            @CurrentMember LoginInfo loginInfo
    ) {
        return reservationService.findMyReservations(loginInfo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDto addReservation(
            @CurrentMember LoginInfo loginInfo,
            @RequestBody final MemberReservationCreateRequestDto requestDto
    ) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(requestDto.date(), requestDto.timeId(),
                requestDto.themeId(), loginInfo.id());
        return reservationService.createReservation(reservationCreateDto);
    }
}
