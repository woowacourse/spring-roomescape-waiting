package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MemberReservationCreateRequestDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationCreateDto;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations() {
        List<ReservationResponseDto> allReservations = reservationService.findAllReservationResponses();
        return ResponseEntity.ok(allReservations);
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservation(@CurrentMember LoginInfo loginInfo, @RequestBody final MemberReservationCreateRequestDto requestDto) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(requestDto.date(), requestDto.timeId(), requestDto.themeId(), loginInfo.id());
        ReservationResponseDto responseDto = reservationService.createReservation(reservationCreateDto);
        return ResponseEntity.created(URI.create("reservations/" + responseDto.id())).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") final Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
