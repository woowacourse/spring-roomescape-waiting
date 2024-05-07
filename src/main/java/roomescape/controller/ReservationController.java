package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.ReservationResponse;
import roomescape.controller.response.ReservationTimeInfoResponse;
import roomescape.model.Reservation;
import roomescape.model.member.LoginMember;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationDto;
import roomescape.service.dto.ReservationTimeInfoDto;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

//@Validated
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<Reservation> reservations = reservationService.findAllReservations();
        List<ReservationResponse> response = reservations.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@Valid @RequestBody ReservationRequest request, LoginMember member) {
        ReservationDto reservationDto = ReservationDto.of(request, member);
        Reservation reservation = reservationService.saveReservation(reservationDto);
        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity
                .created(URI.create("/reservations/" + response.getId()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@NotNull @Min(1) @PathVariable("id") Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/times", params = {"date", "themeId"})
    public ResponseEntity<List<ReservationTimeInfoResponse>> showReservationTimesInformation(@NotNull LocalDate date,
                                                                                             @NotNull @Min(1) Long themeId) {
        ReservationTimeInfoDto timesInfo = reservationService.findReservationTimesInformation(date, themeId);
        List<ReservationTimeInfoResponse> response = ReservationTimeInfoResponse.from(timesInfo);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/filter", params = {"memberId", "themeId", "from", "to"})
    public ResponseEntity<List<ReservationResponse>> searchReservations(@NotNull @Min(1) Long memberId,
                                                                        @NotNull @Min(1) Long themeId,
                                                                        @NotNull LocalDate from,
                                                                        @NotNull LocalDate to) {
        List<Reservation> responses = reservationService.findReservationsByConditions(memberId, themeId, from, to);
        List<ReservationResponse> response = responses.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
