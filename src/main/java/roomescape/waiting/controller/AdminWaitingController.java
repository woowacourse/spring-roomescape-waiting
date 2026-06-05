package roomescape.waiting.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.waiting.service.WaitingApplicationService;
import roomescape.waiting.service.WaitingService;
import roomescape.waiting.service.dto.response.WaitingResponse;

@RestController
@RequestMapping("/admin/waitings")
@RequiredArgsConstructor
public class AdminWaitingController {

    private final WaitingApplicationService waitingApplicationService;

    @GetMapping
    public List<WaitingResponse> findAll() {
        return waitingApplicationService.findAllWithRank().stream()
            .map(waitingWithRank -> WaitingResponse.of(
                waitingWithRank.waiting(),
                waitingWithRank.rank()))
            .toList();
    }
}
