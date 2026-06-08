package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.command.CreateReservationTimeCommand;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.CreateReservationTimeResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/admin/times")
public class AdminReservationTimeController {
    private static final String LOCATION_DEFAULT_VALUE = "/admin/times/";

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<CreateReservationTimeResponse> addReservationTime(
            @Valid @RequestBody ReservationTimeRequest request) {
        CreateReservationTimeCommand command = new CreateReservationTimeCommand(request.startAt());
        CreateReservationTimeResponse response = reservationTimeService.createReservationTime(command);
        return ResponseEntity.created(URI.create(LOCATION_DEFAULT_VALUE + response.id()))
                .body(response);
    }

    @DeleteMapping("/{timeId}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("timeId") Long reservationTimeId) {
        reservationTimeService.deleteReservationTime(reservationTimeId);
        return ResponseEntity.noContent()
                .build();
    }
}
