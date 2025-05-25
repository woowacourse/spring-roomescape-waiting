package roomescape.presentation.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.LoginMember;
import roomescape.dto.request.WaitingRequestDto;
import roomescape.service.WaitingService;

@RequiredArgsConstructor
@RequestMapping("/waiting")
@RestController
public class WaitingController {

    private final WaitingService waitingService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public Long register(@RequestBody @Valid WaitingRequestDto waitingRequestDto, LoginMember loginMember) {
        return waitingService.register(waitingRequestDto, loginMember);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void cancel(@PathVariable("id") Long id) {
        waitingService.cancel(id);
    }
}
