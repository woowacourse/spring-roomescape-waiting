package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;
import static roomescape.auth.domain.AuthRole.MEMBER;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.MemberCreateReservationRequest;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.BookingStatusResponse;
import roomescape.reservation.ui.dto.response.MemberReservationResponse;

@RestController
@RequiredArgsConstructor
public class ReservationRestController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<MemberReservationResponse> create(
            @RequestBody @Valid final MemberCreateReservationRequest request,
            final MemberAuthInfo memberAuthInfo
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createForMember(request, memberAuthInfo.id()));
    }

    @DeleteMapping("/reservations/{id}")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<Void> delete(
            @PathVariable final Long id,
            final MemberAuthInfo memberAuthInfo
    ) {
        reservationService.deleteReservation(id, memberAuthInfo);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations-mine")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<List<MemberReservationResponse>> myReservations(final MemberAuthInfo memberAuthInfo) {
        return ResponseEntity.ok()
                .body(reservationService.findReservationsByMemberId(memberAuthInfo.id()));
    }

    @GetMapping("/reservations/available-times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAvailableReservationTimes(
            @ModelAttribute @Valid final AvailableReservationTimeRequest request) {
        final List<AvailableReservationTimeResponse> availableReservationTimes =
                reservationService.findAvailableReservationTimes(request);

        return ResponseEntity.ok(availableReservationTimes);
    }

    @GetMapping("/reservations/statuses")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<List<BookingStatusResponse>> getBookingStateOptions() {
        return ResponseEntity.ok()
                .body(Arrays.stream(BookingStatus.values())
                        .map(BookingStatusResponse::from)
                        .toList());
    }
}
