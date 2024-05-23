package roomescape.web.api;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.request.time.ReservationTimeRequest;
import roomescape.service.dto.response.time.ReservationTimeResponse;

@RestController
@RequiredArgsConstructor
public class AdminReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    @PostMapping("/admin/times")
    public ResponseEntity<ReservationTimeResponse> saveReservationTime(
            @RequestBody @Valid ReservationTimeRequest request
    ) {
        ReservationTimeResponse response = reservationTimeService.saveReservationTime(request);
        return ResponseEntity.created(URI.create("/times/" + response.id())).body(response);
    }

    @DeleteMapping("/admin/times/{idTime}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable(value = "idTime") Long timeId) {
        reservationTimeService.deleteReservationTime(timeId);
        return ResponseEntity.noContent().build();
    }
}
