package roomescape.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.WaitingService;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRegisterDto;
import roomescape.dto.response.WaitingResponseDto;

@RestController
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping("/waiting")
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingResponseDto registerWaiting(LoginMember loginMember,
                                              @RequestBody @Valid WaitingRegisterDto waitingRegisterDto) {
        return waitingService.registerWaiting(loginMember, waitingRegisterDto);
    }
}
