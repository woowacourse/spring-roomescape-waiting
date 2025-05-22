package roomescape.reservationTime.ui;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationTime.application.ReservationTimeService;
import roomescape.reservationTime.application.dto.TimeRequest;
import roomescape.reservationTime.application.dto.TimeResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/admin/times")
public class AdminReservationTimeController {
    private final ReservationTimeService timeService;

    @PostMapping
    public ResponseEntity<TimeResponse> create(@Valid @RequestBody TimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeService.add(request));
    }

    @GetMapping
    public ResponseEntity<List<TimeResponse>> getAll() {
        return ResponseEntity.ok(timeService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        timeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
