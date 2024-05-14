package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.helper.RoleAllowed;
import roomescape.domain.MemberRole;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.AvailableReservationTimeResponse;
import roomescape.service.dto.ReservationTimeRequest;
import roomescape.service.dto.ReservationTimeResponse;

@RestController
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @RoleAllowed(value = MemberRole.ADMIN)
    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeResponse>> findAllReservationTime() {
        List<ReservationTimeResponse> response = reservationTimeService.findAllReservationTime();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/times/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAllAvailableReservationTime(
            @RequestParam LocalDate date, @RequestParam(name = "theme-id") Long themeId) {
        List<AvailableReservationTimeResponse> response =
                reservationTimeService.findAllAvailableReservationTime(date, themeId);
        return ResponseEntity.ok().body(response);
    }

    @RoleAllowed(value = MemberRole.ADMIN)
    @PostMapping("/times")
    public ResponseEntity<ReservationTimeResponse> saveReservationTime(@RequestBody ReservationTimeRequest request) {
        ReservationTimeResponse response = reservationTimeService.saveReservationTime(request);
        return ResponseEntity.created(URI.create("/times/" + response.getId())).body(response);
    }

    @RoleAllowed(value = MemberRole.ADMIN)
    @DeleteMapping("/times/{timeId}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long timeId) {
        reservationTimeService.deleteReservationTime(timeId);
        return ResponseEntity.noContent().build();
    }
}
