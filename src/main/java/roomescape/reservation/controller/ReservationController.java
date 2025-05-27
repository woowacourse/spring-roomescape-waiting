package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.exception.UnauthorizedAccessException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.UserReservationRequest;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.AutoReserveService;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;
    private final AutoReserveService autoReserveService;

    public ReservationController(ReservationService reservationService, WaitingService waitingService, AutoReserveService autoReserveService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
        this.autoReserveService = autoReserveService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations(Member member) {
        if (Role.isUser(member.getRole())) {
            throw new UnauthorizedAccessException("[ERROR] 접근 권한이 없습니다.");
        }

        List<ReservationResponse> allReservations = reservationService.findAllReservationResponses();
        return ResponseEntity.ok(allReservations);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@Valid @RequestBody final UserReservationRequest request, Member member) {
        ReservationResponse responseDto = reservationService.createUserReservation(request, member);
        return ResponseEntity.created(URI.create("reservations/" + responseDto.id())).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") final Long id, Member member) {
        if (Role.isUser(member.getRole())) {
            throw new UnauthorizedAccessException("[ERROR] 접근 권한이 없습니다.");
        }

        Reservation deletedReservation = reservationService.deleteReservation(id);
        autoReserveService.addReservationFromWaiting(deletedReservation);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> getAllWaitings(Member member) {
        if (Role.isUser(member.getRole())) {
            throw new UnauthorizedAccessException("[ERROR] 접근 권한이 없습니다.");
        }

        return ResponseEntity.ok(waitingService.findAll());
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> addWaiting(@Valid @RequestBody final WaitingRequest request, Member member) {
        WaitingResponse responseDto = waitingService.createWaiting(request, member);
        return ResponseEntity.created(URI.create("reservations/waitings/" + responseDto.id())).body(responseDto);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") final Long id, Member member) {
        waitingService.deleteWaiting(id, member.getId());
        return ResponseEntity.noContent().build();
    }
}
