package roomescape.domain.reservationtime;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservationtime.dto.TimeRequest;
import roomescape.domain.reservationtime.dto.TimeResponse;

@RestController
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping("/times")
    public ResponseEntity<TimeResponse> createTime(
        @RequestBody TimeRequest request
    ) {
        TimeResponse response = reservationTimeService.createTime(request);
        URI location = URI.create("/times/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/times")
    public ResponseEntity<List<TimeResponse>> getTimes() {
        List<TimeResponse> responses = reservationTimeService.getAllTimes();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteTime(
        @PathVariable Long id
    ) {
        reservationTimeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
