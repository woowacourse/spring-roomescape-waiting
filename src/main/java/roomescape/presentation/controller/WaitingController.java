package roomescape.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRequest;
import roomescape.service.WaitingService;

@RequiredArgsConstructor
@RestController
public class WaitingController {

    private final WaitingService waitingService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/waiting")
    public Long register(@RequestBody WaitingRequest waitingRequest, LoginMember loginMember) {
        return waitingService.register(waitingRequest, loginMember);
    }
}
