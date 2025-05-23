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
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations() {
        List<ReservationResponseDto> allReservations = reservationService.findAllReservationResponses();
        return ResponseEntity.ok(allReservations);
    }

    @GetMapping("/me")
    public ResponseEntity<List<MyReservationResponseDto>> getMyReservations(
            @CurrentMember LoginInfo loginInfo
    ) {
        List<MyReservationResponseDto> myReservations = reservationService.findMyReservations(loginInfo);
        return ResponseEntity.ok(myReservations);
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponseDto>> getReservationWaitings() {
        List<ReservationResponseDto> reservationWaitings = reservationService.findAllReservationWaitings();
        return ResponseEntity.ok().body(reservationWaitings);
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservation(
            @CurrentMember LoginInfo loginInfo,
            @RequestBody final MemberReservationCreateRequestDto requestDto
    ) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(requestDto.date(), requestDto.timeId(),
                requestDto.themeId(), loginInfo.id());
        ReservationResponseDto responseDto = reservationService.createReservation(reservationCreateDto);
        return ResponseEntity.created(URI.create("reservations/" + responseDto.id())).body(responseDto);
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponseDto> addReservationWaiting(
            @CurrentMember LoginInfo loginInfo,
            @RequestBody MemberReservationCreateRequestDto request
    ) {
        ReservationCreateDto reservationCreateDto = new ReservationCreateDto(request.date(), request.timeId(),
                request.themeId(), loginInfo.id());
        ReservationResponseDto reservationWaiting = reservationService.createReservationWaiting(reservationCreateDto);
        return ResponseEntity.created(URI.create("reservations/waiting/" + reservationWaiting.id())).body(reservationWaiting);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") final Long id
    ) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(
            @PathVariable("id") final Long id
    ) {
        reservationService.deleteReservationWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
