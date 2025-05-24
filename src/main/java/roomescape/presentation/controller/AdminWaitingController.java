package roomescape.presentation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.WaitingService;

@RequiredArgsConstructor
@RequestMapping("/admin/waitings")
@RestController
public class AdminWaitingController {

    private final WaitingService waitingService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<WaitingResponse> getWaitingList() {
        return waitingService.findAllWaiting();
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    public void deleteWaiting(@PathVariable(name = "id") final Long waitingId) {
        waitingService.cancel(waitingId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{id}")
    public Long approveWaiting(@PathVariable(name = "id") final Long waitingId) {
        return waitingService.approveWaiting(waitingId);
    }
}
