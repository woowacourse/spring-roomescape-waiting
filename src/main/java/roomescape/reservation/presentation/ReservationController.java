package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.Admin;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.presentation.dto.request.ReservationCreateRequest;
import roomescape.reservation.presentation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.presentation.dto.response.ReservationResponse;
import roomescape.reservation.presentation.dto.response.UserReservationResponse;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> saveReservation(
            @RequestBody @Valid ReservationCreateRequest request
    ) {
        ReservationCreateCommand createCommand = new ReservationCreateCommand(
                request.name(),
                request.date(),
                request.timeId(),
                request.themeId()
        );
        ReservationResponse response = ReservationResponse.from(service.saveReservation(createCommand));
        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @Admin
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAdminReservations(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long themeId
    ) {
        List<ReservationResponse> response = findAdminReservations(date, themeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserReservationResponse> getUserReservations(
            @RequestParam String name
    ) {
        UserReservationResponse response = UserReservationResponse.toResponse(service.getReservationsByName(name));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/{id}")
    public ResponseEntity<Void> updateUserReservation(
            @PathVariable Long id,
            @RequestBody @Valid ReservationUpdateRequest request
    ) {
        ReservationUpdateCommand updateCommand = new ReservationUpdateCommand(
                id,
                request.date(),
                request.timeId(),
                request.name()
        );
        service.updateReservationSchedule(updateCommand);
        return ResponseEntity.noContent().build();
    }

    @Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdminReservation(
            @PathVariable Long id
    ) {
        service.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteUserReservation(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        service.cancelReservation(id, name);
        return ResponseEntity.noContent().build();
    }

    private List<ReservationResponse> findAdminReservations(LocalDate date, Long themeId) {
        if (date != null || themeId != null) {
            return service.getReservationsByDateAndTheme(date, themeId).stream()
                    .map(ReservationResponse::from)
                    .toList();
        }
        return service.getReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
