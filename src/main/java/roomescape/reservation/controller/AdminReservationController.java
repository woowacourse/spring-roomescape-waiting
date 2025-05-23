package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.exception.UnauthorizedAccessException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminReservationController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@Valid @RequestBody ReservationRequest request, Member member) {
        if (Role.isUser(member.getRole())) {
            throw new UnauthorizedAccessException("[ERROR] 접근 권한이 없습니다.");
        }

        ReservationResponse responseDto = reservationService.createAdminReservation(request);
        return ResponseEntity.created(URI.create("reservations/" + responseDto.id())).body(responseDto);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> searchReservation(
            @RequestParam(value = "theme-id", required = false) Long themeId,
            @RequestParam(value = "member-id", required = false) Long memberId,
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to,
            Member member) {
        if (Role.isUser(member.getRole())) {
            throw new UnauthorizedAccessException("[ERROR] 접근 권한이 없습니다.");
        }

        return ResponseEntity.ok(reservationService.searchReservations(memberId, themeId, from, to));
    }

    @DeleteMapping("/admin/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        waitingService.deleteWaitingById(id);
        return ResponseEntity.ok().build();
    }
}
