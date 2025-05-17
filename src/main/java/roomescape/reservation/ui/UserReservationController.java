package roomescape.reservation.ui;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.login.application.dto.LoginCheckRequest;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MyReservationResponse;
import roomescape.reservation.application.dto.ReservationResponse;

@RestController
@RequestMapping("/reservations")
public class UserReservationController {

    private final ReservationService reservationService;

    public UserReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> add(
            @Valid @RequestBody final MemberReservationRequest request,
            final LoginCheckRequest loginCheckRequest
    ) {
        final ReservationResponse reservationResponse = reservationService.addMemberReservation(request,
                loginCheckRequest.id());
        return new ResponseEntity<>(reservationResponse, HttpStatus.CREATED);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(final LoginCheckRequest request) {
        List<MyReservationResponse> response = reservationService.findByMemberId(request.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/themes/{themeId}/times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableReservationTime(
            @PathVariable final Long themeId,
            @RequestParam final String date) {
        return ResponseEntity.ok(reservationService.findAvailableReservationTime(themeId, date));
    }
}
