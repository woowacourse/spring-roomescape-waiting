package roomescape.reservationtime.presentation.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.reservationtime.application.ReservationTimeApplicationService;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateWebRequest;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeWebResponse;

@RestController
public class AdminReservationTimeController {

    private final ReservationTimeApplicationService reservationTimeApplicationService;

    public AdminReservationTimeController(final ReservationTimeApplicationService reservationTimeApplicationService) {
        this.reservationTimeApplicationService = reservationTimeApplicationService;
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping("/admin/times")
    public ResponseEntity<ReservationTimeWebResponse> create(
            @RequestBody ReservationTimeCreateWebRequest request
    ) {
        ReservationTimeWebResponse dto = reservationTimeApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/admin/times/{id}")
    public ResponseEntity<Void> remove(
            @PathVariable("id") Long id
    ) {
        reservationTimeApplicationService.removeById(id);
        return ResponseEntity.noContent().build();
    }
}
