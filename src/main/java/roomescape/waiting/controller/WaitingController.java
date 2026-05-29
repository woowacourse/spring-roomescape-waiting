package roomescape.waiting.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.request.WaitingCreateRequest;
import roomescape.waiting.service.dto.response.WaitingCreateResponse;

@RestController
@RequestMapping("/waitings")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping
    public ResponseEntity<WaitingCreateResponse> create(
        @Valid @RequestBody WaitingCreateRequest request
    ) {
        final WaitingCreateResponse response = waitingService.create(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable Long id,
        @RequestParam("customer-name") String customerName
    ) {
        waitingService.deleteByIdAndCustomerName(id, customerName);
        return ResponseEntity.noContent().build();
    }
}
