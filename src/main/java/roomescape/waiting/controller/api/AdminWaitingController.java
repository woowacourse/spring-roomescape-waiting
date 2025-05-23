package roomescape.waiting.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.waiting.controller.dto.response.WaitingWithRankResponse;
import roomescape.waiting.entity.WaitingWithRank;
import roomescape.waiting.service.WaitingService;

import java.util.List;

@RestController
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping("/waiting")
    public List<WaitingWithRankResponse> readAllReservationWaiting() {
        List<WaitingWithRank> allWaiting = waitingService.findAllWaiting();
        return allWaiting.stream()
                .map(WaitingWithRankResponse::from)
                .toList();
    }
}
