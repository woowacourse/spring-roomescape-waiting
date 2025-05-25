package roomescape.presentation.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.WaitingAdminService;

@RestController
@RequestMapping("/admin/waiting")
@RequiredArgsConstructor
public class WaitingAdminController {

    private final WaitingAdminService waitingAdminService;


}
