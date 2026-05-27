package roomescape.reservation.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.CurrentUser;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservation.controller.dto.ReservationEditRequest;
import roomescape.reservation.controller.dto.ReservationWaitingListResponse;
import roomescape.reservation.controller.dto.ReservationWaitingResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.repository.dto.ReservationWaitingResult;

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
    public ResponseEntity<ReservationWaitingListResponse> getMyReservations(@CurrentUser String guestName) {

        return ResponseEntity.ok(
                ReservationWaitingListResponse.from(reservationService.findByGuestName(guestName).stream()
                        .map(ReservationWaitingResponse::from)
                        .toList()));
    }

    @GetMapping("/me/active")
    public ResponseEntity<ReservationWaitingListResponse> getMyActiveReservations(
            @CurrentUser String guestName
    ) {
        var result = reservationService.findByGuestNameExceptCanceled(guestName);

        return ResponseEntity.ok(
                ReservationWaitingListResponse.from(
                        result.stream()
                                .map(ReservationWaitingResponse::from)
                                .toList()
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationWaitingResponse> editDateTime(
            @PathVariable("id") Long id,
            @RequestBody @Valid ReservationEditRequest request,
            @CurrentUser String guestName
    ) {
        ReservationWaitingResult result = reservationService.editDateTime(id, request.date(), request.timeId(), guestName);
        return ResponseEntity.ok(ReservationWaitingResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @CurrentUser String guestName
    ) {
        reservationService.deleteMine(id, guestName);
        return ResponseEntity.noContent().build();
    }

}
