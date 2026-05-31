package roomescape.domain.reservationdate.admin;

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
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationdate.admin.dto.AdminReservationDateResponse;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateRequest;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateResponse;
import roomescape.support.auth.AdminRequestValidator;

@RestController
@RequiredArgsConstructor
public class AdminReservationDateController {

    private final ReservationDateService reservationDateService;
    private final AdminRequestValidator validator;

    @GetMapping("/admin/reservation-dates")
    public ResponseEntity<List<AdminReservationDateResponse>> getAllReservationDateForAdmin(
        HttpServletRequest request
    ) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<AdminReservationDateResponse> response = reservationDateService.getAllReservationDateForAdmin();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/reservation-dates")
    public ResponseEntity<CreateReservationDateResponse> createReservationDate(
        HttpServletRequest httpServletRequest,
        @Valid @RequestBody CreateReservationDateRequest createReservationDateRequest
    ) {
        if (validator.isUnauthorized(httpServletRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CreateReservationDateResponse response = reservationDateService
            .createReservationDate(createReservationDateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/admin/reservation-dates/{id}")
    public ResponseEntity<Void> deleteReservationDate(@PathVariable Long id, HttpServletRequest request) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reservationDateService.deleteReservationDate(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
