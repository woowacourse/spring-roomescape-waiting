package roomescape.time.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.aop.RequiredRoles;
import roomescape.time.application.ReservationTimeFacade;
import roomescape.time.application.dto.ReservationTimeResponse;
import roomescape.user.domain.UserRole;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequiredRoles(UserRole.NORMAL)
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeFacade reservationTimeFacade;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAll() {
        final List<ReservationTimeResponse> reservationTimeResponses = reservationTimeFacade.getAll();
        return ResponseEntity.ok(reservationTimeResponses);
    }
}
