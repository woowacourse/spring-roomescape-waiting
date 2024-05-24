package roomescape.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.ReservationTimeService;
import roomescape.application.ThemeService;
import roomescape.domain.reservation.Status;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.ReservationCriteriaRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final ReservationService reservationService;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public AdminController(
            ReservationService reservationService,
            ThemeService themeService,
            ReservationTimeService reservationTimeService
    ) {
        this.reservationService = reservationService;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<ReservationResponse>> findAllByWaiting() {
        List<ReservationResponse> responses = reservationService.findAllByStatus(Status.WAITING);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservationByAdmin(@RequestBody @Valid AdminReservationRequest adminReservationRequest) {
        ReservationResponse reservationResponse = reservationService.saveByAdmin(adminReservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> searchAdmin(ReservationCriteriaRequest reservationCriteriaRequest) {
        List<ReservationResponse> responses = reservationService.findByCriteria(reservationCriteriaRequest);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> create(@RequestBody ThemeRequest themeRequest) {
        ThemeResponse response = themeService.save(themeRequest);
        URI location = URI.create("/themes/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable long id) {
        themeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/times")
    public ResponseEntity<ReservationTimeResponse> create(@RequestBody ReservationTimeRequest reservationTimeRequest) {
        ReservationTimeResponse response = reservationTimeService.save(reservationTimeRequest);
        URI location = URI.create("/times/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable long id) {
        reservationTimeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
