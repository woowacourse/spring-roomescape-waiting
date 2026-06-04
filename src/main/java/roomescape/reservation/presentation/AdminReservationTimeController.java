package roomescape.reservation.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.presentation.request.CreateTimeRequest;
import roomescape.reservation.presentation.response.CreateTimeResponse;
import roomescape.reservation.presentation.response.ReservationTimeResponse;
import roomescape.common.auth.AdminRequestValidator;

@RestController
@RequiredArgsConstructor
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;
    private final AdminRequestValidator validator;

    @GetMapping("/admin/times")
    public ResponseEntity<List<ReservationTimeResponse>> getAllReservationTime(HttpServletRequest request) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ReservationTimeResponse> response = reservationTimeService.getAllReservationTime();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/times")
    public ResponseEntity<CreateTimeResponse> createReservationTime(
        HttpServletRequest httpServletRequest,
        @Valid @RequestBody CreateTimeRequest createTimeRequest
    ) {
        if (validator.isUnauthorized(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CreateTimeResponse response = reservationTimeService.createReservationTime(createTimeRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/times/{id}")
    public ResponseEntity<Void> deleteReservationTime(HttpServletRequest request, @PathVariable Long id) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.ok().build();
    }
}
