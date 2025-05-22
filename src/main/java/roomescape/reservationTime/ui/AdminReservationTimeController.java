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
import roomescape.common.response.ApiResponse;
import roomescape.reservationTime.application.ReservationTimeService;
import roomescape.reservationTime.application.dto.TimeRequest;
import roomescape.reservationTime.application.dto.TimeResponse;

@RestController
@AllArgsConstructor
@RequestMapping("admin/times")
public class AdminReservationTimeController {
    private final ReservationTimeService timeService;

    @PostMapping
    public ResponseEntity<ApiResponse<TimeResponse>> create(@Valid @RequestBody TimeRequest request) {
        TimeResponse response = timeService.create(request);
        ApiResponse<TimeResponse> apiResponse = ApiResponse.createSuccess(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeResponse>>> getAll() {
        List<TimeResponse> response = timeService.findAll();
        ApiResponse<List<TimeResponse>> apiResponse = ApiResponse.createSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        timeService.deleteById(id);
        ApiResponse<Void> apiResponse = ApiResponse.createSuccessWithNoData();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
