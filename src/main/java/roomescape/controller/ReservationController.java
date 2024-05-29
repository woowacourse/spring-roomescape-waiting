package roomescape.controller;

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

import roomescape.domain.LoginMember;
import roomescape.dto.AdminReservationDetailResponse;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.ReservationDetailResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> saveReservation(@Authenticated LoginMember loginMember,
                                                               @RequestBody ReservationRequest reservationRequest) {
        ReservationResponse savedReservationResponse = reservationService.save(loginMember, reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + savedReservationResponse.id()))
                .body(savedReservationResponse);
    }

    @PostMapping("/reservations-waiting")
    public ResponseEntity<ReservationResponse> saveReservationWaiting(@Authenticated LoginMember loginMember,
                                                                      @RequestBody ReservationRequest reservationRequest) {
        ReservationResponse savedReservationResponse = reservationService.saveWaiting(loginMember, reservationRequest);
        return ResponseEntity.created(URI.create("/reservations-waiting/" + savedReservationResponse.id()))
                .body(savedReservationResponse);
    }

    @PostMapping("admin/reservations")
    public ResponseEntity<ReservationResponse> saveReservationByAdmin(
            @RequestBody AdminReservationRequest reservationRequest) {
        ReservationResponse reservationResponse = reservationService.saveByAdmin(reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> findAllReservations() {
        return reservationService.findAllReservations();
    }

    @GetMapping("/member/reservation")
    public List<ReservationDetailResponse> findMemberReservations(@Authenticated LoginMember loginMember) {
        return reservationService.findAllByMemberId(loginMember.getId());
    }

    @GetMapping("/reservations/search")
    public List<ReservationResponse> searchReservation(@RequestParam Long themeId,
                                                       @RequestParam Long memberId,
                                                       @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}")
                                                       LocalDate dateFrom,
                                                       @RequestParam(defaultValue = "#{T(java.time.LocalDate).now()}")
                                                       LocalDate dateTo) {
        return reservationService.searchReservation(themeId, memberId, dateFrom, dateTo);
    }

    @GetMapping("admin/reservations-waiting")
    public List<AdminReservationDetailResponse> findAllWaitingReservations() {
        return reservationService.findAllWaitingReservations();
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reservations-waiting/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(@Authenticated LoginMember loginMember, @PathVariable long id) {
        reservationService.deleteByMemberIdAndId(loginMember, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/reservations-waiting/{id}")
    public ResponseEntity<Void> deleteReservationWaitingByAdmin(@PathVariable long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
