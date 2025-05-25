package roomescape.presentation.controller.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.WaitingAdminService;
import roomescape.dto.response.WaitingAdminResponseDto;

@RestController
@RequestMapping("/admin/waiting")
@RequiredArgsConstructor
public class WaitingAdminController {

    private final WaitingAdminService waitingAdminService;

    public List<WaitingAdminResponseDto> getWaitings() {
        return waitingAdminService.getAllWaitings();
    }
}
