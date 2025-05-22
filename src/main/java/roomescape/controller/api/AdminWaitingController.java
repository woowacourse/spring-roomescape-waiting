package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservation.ReservationAndWaitingResponseDto;
import roomescape.dto.waiting.WaitingResponseDto;
import roomescape.service.waiting.WaitingQueryService;

import java.util.List;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingQueryService waitingQueryService;

    public AdminWaitingController(WaitingQueryService waittingQueryService) {
        this.waitingQueryService = waittingQueryService;
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponseDto>> getWaitings() {
        List<WaitingResponseDto> allWaiting = waitingQueryService.findAllWaiting();
        return ResponseEntity.ok(allWaiting);
    }
}
