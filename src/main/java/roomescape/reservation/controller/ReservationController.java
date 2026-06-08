package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.common.auth.CurrentUser;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.ReservationWaitingResult;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> create(@RequestBody @Valid ReservationCreateRequest request) {
        ReservationWaitingResult reservationWaitingResult = reservationService.create(
                request.guestName(),
                request.date(),
                request.timeId(),
                request.themeId()
        );

        return ResponseEntity.status(CREATED)
                .body(ReservationWaitingResponse.from(reservationWaitingResult));
    }

    @GetMapping("/me")
    public ResponseEntity<ReservationWaitingListResponse> getListByGuestName(@CurrentUser String guestName) {
        return ResponseEntity.ok(
                ReservationWaitingListResponse.from(reservationService.findByGuestName(guestName).stream()
                        .map(ReservationWaitingResponse::from)
                        .toList()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> editDateTime(
            @PathVariable("id") Long id,
            @RequestBody @Valid ReservationEditRequest request,
            @CurrentUser String guestName
    ) {
        reservationService.editDateTime(id, request.date(), request.timeId(), guestName);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @CurrentUser String guestName
    ) {
        reservationService.cancelMine(id, guestName);
        return ResponseEntity.noContent().build();
    }
}
