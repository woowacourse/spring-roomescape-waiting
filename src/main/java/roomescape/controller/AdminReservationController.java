package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.CreateReservationAdminRequest;
import roomescape.controller.dto.response.BookingResponse;
import roomescape.service.ReservationService;
import roomescape.service.dto.param.CreateBookingParam;
import roomescape.service.dto.result.ReservationResult;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public BookingResponse createReservation(@Valid @RequestBody CreateReservationAdminRequest reservationRequest) {
        CreateBookingParam createBookingParam = new CreateBookingParam(
                reservationRequest.memberId(),
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId()
        );
        ReservationResult reservationResult = reservationService.create(createBookingParam);
        return BookingResponse.from(reservationResult);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long reservationId) {
        reservationService.deleteByIdAndReserveNextWaiting(reservationId);
        return ResponseEntity.noContent().build();
    }
}
