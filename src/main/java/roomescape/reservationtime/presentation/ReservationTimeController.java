package roomescape.reservationtime.presentation;

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
import roomescape.common.security.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.reservationtime.application.ReservationTimeApplicationService;
import roomescape.reservationtime.presentation.dto.request.ReservationTimeCreateWebRequest;
import roomescape.reservationtime.presentation.dto.response.AvailableReservationTimeWebResponse;
import roomescape.reservationtime.presentation.dto.response.ReservationTimeWebResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeApplicationService reservationTimeApplicationService;

    public ReservationTimeController(final ReservationTimeApplicationService reservationTimeApplicationService) {
        this.reservationTimeApplicationService = reservationTimeApplicationService;
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping
    public ResponseEntity<ReservationTimeWebResponse> create(
            @RequestBody ReservationTimeCreateWebRequest request
    ) {
        ReservationTimeWebResponse dto = reservationTimeApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeWebResponse>> findAll() {
        return ResponseEntity.ok(reservationTimeApplicationService.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeWebResponse>> findAvailable(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId
    ) {
        return ResponseEntity.ok(reservationTimeApplicationService.findAvailable(date, themeId));
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(
            @PathVariable("id") Long id
    ) {
        reservationTimeApplicationService.removeById(id);
        return ResponseEntity.noContent().build();
    }
}
