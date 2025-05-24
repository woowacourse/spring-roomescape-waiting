package roomescape.controller.waiting;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.waiting.ApplyWaitingRequestDto;
import roomescape.dto.waiting.WaitingResponseDto;
import roomescape.infrastructure.auth.intercept.AuthenticationPrincipal;
import roomescape.infrastructure.auth.member.UserInfo;
import roomescape.service.waiting.WaitingService;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponseDto> addWaiting(@RequestBody @Valid AddReservationDto newReservationDto,
                                                         @AuthenticationPrincipal UserInfo userInfo) {
        long addedWaitingId = waitingService.addWaiting(newReservationDto, userInfo.id());
        Waiting waiting = waitingService.getWaitingById(addedWaitingId);

        WaitingResponseDto waitingResponseDto = new WaitingResponseDto(addedWaitingId, waiting.getName(), waiting.getStartAt(), waiting.getDate(), waiting.getThemeName());
        return ResponseEntity.created(URI.create("/waitings/" + addedWaitingId)).body(waitingResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponseDto>> waitings() {
        List<Waiting> waitings = waitingService.getAllWaitings();
        List<WaitingResponseDto> waitingResponseDtos = waitings.stream()
                .map(waiting -> new WaitingResponseDto(waiting.getId(), waiting.getName(), waiting.getStartAt(), waiting.getDate(), waiting.getThemeName()))
                .toList();
        return ResponseEntity.ok(waitingResponseDtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingService.deleteWaiting(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/apply")
    public ResponseEntity<Void> applyWaiting(@RequestBody ApplyWaitingRequestDto applyWaitingRequestDto) {
        Long reservationId = waitingService.apply(applyWaitingRequestDto);
        return ResponseEntity.created(URI.create("/reservations/" + reservationId)).build();
    }
}
