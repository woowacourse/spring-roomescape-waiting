package roomescape.waiting.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.user.domain.User;
import roomescape.waiting.domain.dto.WaitingRequestDto;
import roomescape.waiting.domain.dto.WaitingResponseDto;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("reservation-waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public HttpEntity<WaitingResponseDto> create(@RequestBody WaitingRequestDto requestDto, User member) {
        WaitingResponseDto responseDto = waitingService.create(requestDto, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
