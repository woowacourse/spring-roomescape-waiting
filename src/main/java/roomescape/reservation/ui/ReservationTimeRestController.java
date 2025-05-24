package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.ui.dto.request.CreateReservationTimeRequest;
import roomescape.reservation.ui.dto.response.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class ReservationTimeRestController {

    private final ReservationTimeService reservationTimeService;

    @PostMapping
    @RequiresRole(authRoles = {ADMIN})
    public ResponseEntity<ReservationTimeResponse> create(
            @RequestBody @Valid final CreateReservationTimeRequest request
    ) {
        final ReservationTimeResponse response = reservationTimeService.create(request);

        return ResponseEntity.created(URI.create("/times/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    @RequiresRole(authRoles = {ADMIN})
    public ResponseEntity<Void> delete(
            @PathVariable final Long id
    ) {
        reservationTimeService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        return ResponseEntity.ok(reservationTimeService.findAll());
    }
}
