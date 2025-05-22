package roomescape.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.controller.dto.request.CreateBookingRequest;
import roomescape.controller.dto.request.LoginMemberInfo;
import roomescape.controller.dto.response.BookingResponse;
import roomescape.service.ReservationCreationService;
import roomescape.service.ReservationService;
import roomescape.service.dto.result.ReservationResult;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationCreationService reservationCreationService;
    private final ReservationService reservationService;


    public ReservationController(ReservationCreationService reservationCreationService,
                                 ReservationService reservationService) {
        this.reservationCreationService = reservationCreationService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getReservations(@RequestParam(required = false) Long memberId,
                                                                 @RequestParam(required = false) Long themeId,
                                                                 @RequestParam(required = false) LocalDate dateFrom,
                                                                 @RequestParam(required = false) LocalDate dateTo) {
        List<ReservationResult> results = reservationService.getReservationsInConditions(memberId, themeId, dateFrom, dateTo);
        return ResponseEntity.ok(BookingResponse.fromReservations(results));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createReservation(
            @Valid @RequestBody CreateBookingRequest createBookingRequest,
            @LoginMember LoginMemberInfo loginMemberInfo) {

        ReservationResult reservationResult = reservationCreationService.create(createBookingRequest.toServiceParam(loginMemberInfo.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(reservationResult));
    }
}
