package roomescape.reservationwaiting.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationwaiting.dto.AdminWaitingResponse;
import roomescape.reservationwaiting.service.ReservationWaitingService;

@Tag(name = "어드민 예약 대기", description = "예약 대기 전체 조회·취소 API (관리자용)")
@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public AdminWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping
    public ResponseEntity<List<AdminWaitingResponse>> getWaitings() {
        List<AdminWaitingResponse> responses = reservationWaitingService.getAllWaitings()
                .stream()
                .map(AdminWaitingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        reservationWaitingService.deleteWaitingByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}