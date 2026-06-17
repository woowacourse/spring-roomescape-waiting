package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Order;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.dto.reservation.command.DeleteReservationCommand;
import roomescape.dto.reservation.command.CreateReservationCommand;
import roomescape.dto.reservation.request.CreateReservationRequest;
import roomescape.dto.reservation.response.ReservationResponse;
import roomescape.dto.reservation.response.ReservationWithStatusResponses;
import roomescape.dto.reservation.command.UpdateReservationCommand;
import roomescape.dto.reservation.request.UpdateReservationRequest;
import roomescape.domain.User;
import roomescape.infrastructure.LoginRequired;
import roomescape.infrastructure.LoginUser;
import roomescape.service.OrderService;
import roomescape.service.ReservationService;

@Validated
@RestController
@RequestMapping("/reservations")
@LoginRequired
public class ReservationController {

    private final ReservationService reservationService;
    private final OrderService orderService;

    public ReservationController(ReservationService reservationService, OrderService orderService) {
        this.reservationService = reservationService;
        this.orderService = orderService;
    }

    @GetMapping("/mine")
    public ResponseEntity<ReservationWithStatusResponses> readMyReservations(
            @LoginUser User loginUser,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ResponseEntity.ok(reservationService.getMyReservationStatuses(loginUser, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> readReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(ReservationResponse.from(reservationService.getReservation(id)));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @LoginUser User loginUser,
            @Valid @RequestBody CreateReservationRequest request) {
        Reservation createdReservation = reservationService.create(
                CreateReservationCommand.of(loginUser, request), ReservationStatus.RESERVED);
        Order createdOrder = orderService.create(createdReservation);
        URI location = URI.create("/reservations/" + createdReservation.getId());
        return ResponseEntity.created(location).body(ReservationResponse.from(createdReservation));
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> createWaitingReservation(
            @LoginUser User loginUser,
            @Valid @RequestBody CreateReservationRequest request
    ) {
        Reservation createdWaiting = reservationService.create(
                CreateReservationCommand.of(loginUser, request), ReservationStatus.WAITING);

        URI location = URI.create("/reservations/" + createdWaiting.getId());
        return ResponseEntity.created(location).body(ReservationResponse.from(createdWaiting));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @LoginUser User loginUser,
            @Valid @RequestBody UpdateReservationRequest request) {
        Reservation updated = reservationService.updateOwnReservation(
                UpdateReservationCommand.of(id, loginUser, request));
        return ResponseEntity.ok(ReservationResponse.from(updated));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            @LoginUser User loginUser) {
        reservationService.deleteOwnReservation(DeleteReservationCommand.of(id, loginUser));
        return ResponseEntity.ok().build();
    }
}
