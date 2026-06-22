package roomescape.feature.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import roomescape.feature.payment.dto.PaymentApproveRequest;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.feature.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.service.ReservationService;
import roomescape.global.ratelimit.InboundRateLimit;

@RestController
@RequestMapping("/api/reservations")
@Validated
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> getReservationsByName(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(reservationService.getReservationsByName(new ReserverName(name)));
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponseDto> saveReservation(
            @Valid @RequestBody ReservationCreateRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.saveReservation(reservationMapper.toCreateCommand(requestDto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationCreateResponseDto> updateReservation(
            @PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id,
            @Valid @RequestBody ReservationUpdateRequestDto requestDto
    ) {
        return ResponseEntity.ok(
                reservationService.updateReservation(id, reservationMapper.toUpdateCommand(requestDto)));
    }

    @PostMapping("/{id}/payment")
    @InboundRateLimit(key = "payment_inbound")
    public ResponseEntity<Void> confirmReservation(
            @PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id,
            @RequestBody PaymentApproveRequest requestDto
    ) {
        reservationService.confirmReservation(id, requestDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationCancelResponseDto> cancelReservation(
            @PathVariable @Positive(message = "id의 값은 양수여야 합니다.") Long id,
            @RequestParam String name
    ) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, new ReserverName(name)));
    }
}
