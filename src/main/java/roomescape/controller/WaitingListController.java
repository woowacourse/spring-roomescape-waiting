package roomescape.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.WaitingListService;

@RequiredArgsConstructor
@RequestMapping("/waiting-list")
@RestController
public class WaitingListController {

    private final WaitingListService waitingListService;

}
