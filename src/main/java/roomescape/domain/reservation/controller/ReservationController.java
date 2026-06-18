package roomescape.domain.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.mapper.ReservationMapper;
import roomescape.domain.reservation.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
@Validated
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    public ReservationController(ReservationService reservationService, ReservationMapper reservationMapper) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getReservationsByName(
        @RequestParam
        @NotBlank(message = "예약자명은 필수입니다.")
        @Size(max = 20, message = "예약자명의 길이는 1이상 20이하 입니다.")
        String name
    ) {
        return ResponseEntity.ok(reservationService.getReservationsByName(name));
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponseDto> saveReservation(
        @Valid @RequestBody ReservationCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reservationService.saveReservation(reservationMapper.toCreateCommand(requestDto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationCreateResponseDto> updateReservation(
        @PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id,
        @RequestParam
        @NotBlank(message = "예약자명은 필수입니다.")
        @Size(max = 20, message = "예약자명의 길이는 1이상 20이하 입니다.")
        String name,
        @Valid @RequestBody ReservationUpdateRequestDto requestDto) {
        return ResponseEntity.ok(
            reservationService.updateReservation(id, name,
                reservationMapper.toUpdateCommand(requestDto)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationCancelResponseDto> cancelReservation(
        @PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id,
        @RequestParam
        @NotBlank(message = "예약자명은 필수입니다.")
        @Size(max = 20, message = "예약자명의 길이는 1이상 20이하 입니다.")
        String name
    ) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, name));
    }

    @PostMapping("/waitings")
    public ResponseEntity<ReservationCreateResponseDto> saveWaitingReservation(
        @Valid @RequestBody ReservationCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reservationService.saveWaitingReservation(reservationMapper.toCreateCommand(requestDto)));
    }

    @PatchMapping("/{id}/waitings/cancel")
    public ResponseEntity<ReservationCancelResponseDto> cancelWaitingReservation(
        @PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id,
        @RequestParam
        @NotBlank(message = "예약자명은 필수입니다.")
        @Size(max = 20, message = "예약자명의 길이는 1이상 20이하 입니다.")
        String name
    ) {
        return ResponseEntity.ok(reservationService.cancelWaitingReservation(id, name));
    }
}
