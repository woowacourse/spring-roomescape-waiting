package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.WaitingRequest;
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.service.WaitingService;

@RestController
@RequestMapping("/waitings")
@Validated
public class WaitingController {

    private static final String MEMBER_NAME_HEADER = "X-Member-Name";

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingReservationResponse> createWaiting(
            @NotBlank @RequestHeader(MEMBER_NAME_HEADER) String memberName,
            @Valid @RequestBody WaitingRequest waitingRequest
    ) {
        WaitingReservationResponse response = waitingService.saveWaiting(memberName, waitingRequest.themeSlotId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable Long id,
            @NotBlank @RequestHeader(MEMBER_NAME_HEADER) String memberName
    ) {
        waitingService.deleteWaiting(id, memberName);
        return ResponseEntity.noContent().build();
    }
}
