package roomescape.reservation.ui;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.login.application.dto.LoginCheckRequest;
import roomescape.reservation.application.ReservationCommandService;
import roomescape.reservation.application.ReservationQueryService;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MyReservationResponse;
import roomescape.reservation.application.dto.ReservationResponse;

@RestController
@RequestMapping("/reservations")
public class UserReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    public UserReservationController(
            final ReservationCommandService reservationCommandService,
            final ReservationQueryService reservationQueryService
    ) {
        this.reservationCommandService = reservationCommandService;
        this.reservationQueryService = reservationQueryService;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(final LoginCheckRequest request) {
        List<MyReservationResponse> response = reservationQueryService.findByMemberId(request.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/themes/{themeId}/times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableReservationTime(
            @PathVariable final Long themeId,
            @RequestParam final LocalDate date
    ) {
        return ResponseEntity.ok(reservationQueryService.findAvailableReservationTime(themeId, date));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> add(
            @Valid @RequestBody final MemberReservationRequest request,
            final LoginCheckRequest loginCheckRequest
    ) {
        final ReservationResponse reservationResponse = reservationCommandService.addMemberReservation(request,
                loginCheckRequest.id());
        return new ResponseEntity<>(reservationResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") final Long id,
            final LoginCheckRequest request
    ) {
        reservationCommandService.deleteById(id, request.id());
        return ResponseEntity.noContent().build();
    }
}
