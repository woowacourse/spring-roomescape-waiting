package roomescape.time.ui;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.aop.RequiredRoles;
import roomescape.common.uri.UriFactory;
import roomescape.time.application.ReservationTimeFacade;
import roomescape.time.application.dto.ReservationTimeResponse;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.ui.dto.CreateReservationTimeWebRequest;
import roomescape.user.domain.UserRole;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequiredRoles(UserRole.ADMIN)
@RequestMapping(ReservationTimeController.BASE_PATH)
public class ReservationTimeController {

    public static final String BASE_PATH = "/times";

    private final ReservationTimeFacade reservationTimeFacade;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAll() {
        final List<ReservationTimeResponse> reservationTimeResponses = reservationTimeFacade.getAll();
        return ResponseEntity.ok(reservationTimeResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @RequestBody final CreateReservationTimeWebRequest request) {
        final ReservationTimeResponse reservationTimeResponse = reservationTimeFacade.create(request.toServiceRequest());
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(reservationTimeResponse.id()));
        return ResponseEntity.created(location)
                .body(reservationTimeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        reservationTimeFacade.delete(ReservationTimeId.from(id));
        return ResponseEntity.noContent().build();
    }
}
