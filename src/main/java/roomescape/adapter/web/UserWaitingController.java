package roomescape.adapter.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.adapter.web.dto.request.WaitingRequest;
import roomescape.adapter.web.dto.response.WaitingResponse;
import roomescape.application.WaitingService;
import roomescape.application.dto.result.WaitingResult;

@Validated
@RestController
@RequestMapping("/user/waitings")
public class UserWaitingController {

    private final WaitingService waitingService;

    public UserWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingResponse create(@RequestBody @Valid WaitingRequest request) {
        WaitingResult saved = waitingService.create(request.toCommand());
        return WaitingResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, @RequestParam @NotBlank(message = "이름은 비어 있을 수 없습니다.") String name) {
        waitingService.cancelByOwner(id, name);
    }
}
