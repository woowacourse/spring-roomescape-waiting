package roomescape.admin.controller;

import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.domain.dto.AdminReservationRequestDto;
import roomescape.admin.domain.dto.SearchReservationRequestDto;
import roomescape.admin.service.AdminService;
import roomescape.reservation.domain.dto.ReservationResponseDto;
import roomescape.waiting.domain.dto.WaitingResponseDto;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponseDto> createReservation(
            @RequestBody AdminReservationRequestDto requestDto) {
        ReservationResponseDto reservationResponseDto = adminService.createReservation(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponseDto>> searchReservations(SearchReservationRequestDto requestDto) {
        List<ReservationResponseDto> reservationResponseDtos = adminService.searchReservations(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(reservationResponseDtos);
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponseDto>> findAllWaitings() {
        List<WaitingResponseDto> responseDtos = adminService.findAllWaitings();
        return ResponseEntity.status(HttpStatus.OK).body(responseDtos);
    }

    @DeleteMapping("/waitings/{id}")
    public HttpEntity<Void> delete(@PathVariable(value = "id") Long waitingId) {
        adminService.deleteWaitingById(waitingId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
