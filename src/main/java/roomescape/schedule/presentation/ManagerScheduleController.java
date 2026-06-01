package roomescape.schedule.presentation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.schedule.application.ScheduleService;
import roomescape.schedule.dto.request.ScheduleSaveRequest;
import roomescape.schedule.dto.response.ScheduleFindResponse;
import roomescape.schedule.dto.response.ScheduleSaveResponse;

import java.util.List;

@RestController
@RequestMapping("/api/manager/schedules")
@RequiredArgsConstructor
public class ManagerScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleSaveResponse>> save(
            @RequestBody @Valid ScheduleSaveRequest body
    ) {
        ScheduleSaveResponse response = scheduleService.save(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduleFindResponse>>> findAll() {
        List<ScheduleFindResponse> response = scheduleService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleFindResponse>> findById(
            @PathVariable @Positive long scheduleId
    ) {
        ScheduleFindResponse response = scheduleService.findById(scheduleId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive long scheduleId
    ) {
        scheduleService.deleteById(scheduleId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }
}
