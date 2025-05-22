package roomescape.waiting.ui;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.response.ApiResponse;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.WaitingResponse;

@RestController
@AllArgsConstructor
@RequestMapping("admin/waitings")
public class AdminWaitingController {
    private final WaitingService waitingService;

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable("id") Long id) {
        waitingService.approve(id);
        ApiResponse<Void> apiResponse = ApiResponse.createSuccessWithNoData();
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WaitingResponse>>> getAll() {
        List<WaitingResponse> response = waitingService.findAll();
        ApiResponse<List<WaitingResponse>> apiResponse = ApiResponse.createSuccess(response);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        waitingService.deleteByAdmin(id);
        ApiResponse<Void> apiResponse = ApiResponse.createSuccessWithNoData();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
