package roomescape.controller.reservationtime;

import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.auth.RoleAllowed;
import roomescape.domain.member.MemberRole;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.reservationtime.dto.ReservationTimeAvailableListResponse;
import roomescape.service.reservationtime.dto.ReservationTimeListResponse;
import roomescape.service.reservationtime.dto.ReservationTimeRequest;
import roomescape.service.reservationtime.dto.ReservationTimeResponse;

@RestController
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @RoleAllowed(value = MemberRole.ADMIN)
    @GetMapping("/times")
    public ResponseEntity<ReservationTimeListResponse> findAllReservationTime() {
        ReservationTimeListResponse response = reservationTimeService.findAllReservationTime();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/times/available")
    public ResponseEntity<ReservationTimeAvailableListResponse> findAllAvailableReservationTime(
            @RequestParam LocalDate date, @RequestParam Long themeId) {
        ReservationTimeAvailableListResponse response =
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
