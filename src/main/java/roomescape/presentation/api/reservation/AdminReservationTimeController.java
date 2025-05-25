package roomescape.presentation.api.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.command.CreateReservationTimeService;
import roomescape.application.reservation.command.DeleteReservationTimeService;
import roomescape.presentation.api.reservation.request.CreateReservationTimeRequest;

@RestController
@RequestMapping("/admin/times")
public class AdminReservationTimeController {

    private static final String RESERVATION_TIMES_URL = "/times/%d";

    private final CreateReservationTimeService createReservationTimeService;
    private final DeleteReservationTimeService deleteReservationTimeService;

    public AdminReservationTimeController(CreateReservationTimeService createReservationTimeService,
                                          DeleteReservationTimeService deleteReservationTimeService) {
        this.createReservationTimeService = createReservationTimeService;
        this.deleteReservationTimeService = deleteReservationTimeService;
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody CreateReservationTimeRequest createReservationTImeRequest) {
        Long id = createReservationTimeService.register(createReservationTImeRequest.toCreateCommand());
        return ResponseEntity.created(URI.create(RESERVATION_TIMES_URL.formatted(id)))
                .build();
    }

    @DeleteMapping("/{timeId}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("timeId") Long reservationTimeId) {
        deleteReservationTimeService.removeById(reservationTimeId);
        return ResponseEntity.noContent().build();
    }
}
