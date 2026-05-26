package roomescape.wating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.wating.service.WaitingService;
import roomescape.wating.service.dto.request.WaitingCreateRequest;
import roomescape.wating.service.dto.response.WaitingCreateResponse;

@Controller
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

}
