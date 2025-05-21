package roomescape.waiting.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static roomescape.waiting.controller.response.WaitingSuccessCode.DELETE_WAITING_SUCCESS_CODE;
import static roomescape.waiting.controller.response.WaitingSuccessCode.READ_WAITING_SUCCESS_CODE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.response.ApiResponse;
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/admin/waitings")
@RequiredArgsConstructor
public class WaitingAdminApiController {

    private final WaitingService waitingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WaitingInfoResponse>>> readAll() {
        List<WaitingInfoResponse> responses = waitingService.getAll();

        return ResponseEntity.ok(ApiResponse.success(READ_WAITING_SUCCESS_CODE, responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWaiting(@PathVariable Long id) {
        waitingService.delete(id);

        return ResponseEntity
                .status(NO_CONTENT)
                .body(ApiResponse.success(DELETE_WAITING_SUCCESS_CODE));
    }
}
