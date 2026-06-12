package roomescape.wating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.wating.service.WaitingService;
import roomescape.wating.controller.dto.request.WaitingCreateRequest;
import roomescape.wating.controller.dto.response.WaitingCreateResponse;

@RestController
@RequestMapping("/waitings")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<WaitingCreateResponse> create(
            @Valid @RequestBody WaitingCreateRequest request
    ) {
        final long savedId = waitingService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new WaitingCreateResponse(savedId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam("customer-name") String customerName,
            @RequestParam("customer-email") String customerEmail
    ) {
        waitingService.deleteByIdAndCustomer(id, customerName, customerEmail);
        return ResponseEntity.noContent().build();
    }
}
