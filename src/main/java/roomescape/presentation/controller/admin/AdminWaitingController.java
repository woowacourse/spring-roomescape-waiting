package roomescape.presentation.controller.admin;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.WaitingService;
import roomescape.application.dto.WaitingServiceResponse;
import roomescape.presentation.controller.admin.dto.AdminWaitingResponse;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public List<AdminWaitingResponse> getAllWaitings() {
        List<WaitingServiceResponse> waitings = waitingService.getAllWaitings();
        return AdminWaitingResponse.from(waitings);
    }
}
