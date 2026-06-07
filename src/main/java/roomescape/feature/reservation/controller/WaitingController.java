package roomescape.feature.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.service.WaitingService;

@RestController
@RequestMapping("/api/reservations/waitings")
@Validated
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;
    private final ReservationMapper reservationMapper;

    @PostMapping
    public ResponseEntity<ReservationCreateResponseDto> saveWaitingReservation(
        @Valid @RequestBody ReservationCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(waitingService.saveWaitingReservation(reservationMapper.toCreateCommand(requestDto)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationCancelResponseDto> cancelWaitingReservation(
        @PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id,
        @RequestParam String name
    ) {
        return ResponseEntity.ok(waitingService.cancelWaitingReservation(id, new ReserverName(name)));
    }
}
