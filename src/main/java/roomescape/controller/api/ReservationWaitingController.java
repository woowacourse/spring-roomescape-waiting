package roomescape.controller.api;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<List<ReservationResponseDto>> getReservationWaitings() {
        List<ReservationResponseDto> reservationWaitings = reservationWaitingService.findAllReservationWaitings();
        return ResponseEntity.ok().body(reservationWaitings);
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponseDto> addReservationWaiting(
            @CurrentMember LoginInfo loginInfo,
            @RequestBody MemberReservationCreateRequestDto request
    ) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(request.date(), request.timeId(),
                request.themeId(), loginInfo.id());
        ReservationResponseDto reservationWaiting = reservationWaitingService.createReservationWaiting(reservationCreateDto);
        return ResponseEntity.created(URI.create("reservations/waiting/" + reservationWaiting.id())).body(reservationWaiting);
    }


    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(
            @PathVariable("id") final Long id
    ) {
        reservationWaitingService.deleteReservationWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
