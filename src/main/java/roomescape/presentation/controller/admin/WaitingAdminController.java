package roomescape.presentation.controller.admin;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.WaitingAdminService;
import roomescape.dto.response.WaitingAdminResponseDto;

@RestController
@RequestMapping("/admin/waitings")
@RequiredArgsConstructor
public class WaitingAdminController {

    private final WaitingAdminService waitingAdminService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<WaitingAdminResponseDto> getWaitings() {
        return waitingAdminService.getAllWaitings();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectWaiting(@PathVariable Long id) {
        waitingAdminService.rejectWaiting(id);
    }
}
