package roomescape.waiting.controller;

import static roomescape.waiting.controller.response.WaitingSuccessCode.READ_WAITING_SUCCESS_CODE;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.response.ApiResponse;
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.service.WaitingQueryService;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/admin/waitings")
@RequiredArgsConstructor
public class WaitingAdminApiController {

    private final WaitingService waitingService;
    private final WaitingQueryService waitingQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WaitingInfoResponse>>> readAll(Pageable pageable) {
        Page<WaitingInfoResponse> responses = waitingQueryService.getAllInfo(pageable);

        return ResponseEntity.ok(ApiResponse.success(READ_WAITING_SUCCESS_CODE, responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWaiting(@PathVariable Long id) {
        waitingService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
