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
import roomescape.service.dto.ReservationCreateDto;
import roomescape.service.reservation.ReservationCommandService;
import roomescape.service.reservation.ReservationQueryService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;

    public ReservationController(ReservationQueryService reservationQueryService, ReservationCommandService reservationCommandService) {
        this.reservationQueryService = reservationQueryService;
        this.reservationCommandService = reservationCommandService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations() {
        List<ReservationResponseDto> allReservations = reservationQueryService.findAllReservationResponses();
        return ResponseEntity.ok(allReservations);
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservation(@CurrentMember LoginInfo loginInfo,
                                                                 @RequestBody final MemberReservationCreateRequestDto requestDto) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(requestDto.date(), requestDto.timeId(),
                requestDto.themeId(), loginInfo.id());
        ReservationResponseDto responseDto = reservationCommandService.createReservation(reservationCreateDto);
        return ResponseEntity.created(URI.create("reservations/" + responseDto.id())).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") final Long id) {
        reservationCommandService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
