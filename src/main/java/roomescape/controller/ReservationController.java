package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.OwnReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.controller.response.ReservationTimeInfoResponse;
import roomescape.model.Reservation;
import roomescape.model.WaitingWithRank;
import roomescape.model.member.LoginMember;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.service.dto.ReservationDto;
import roomescape.service.dto.ReservationTimeInfoDto;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
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
        ReservationDto reservationDto = request.toDto(member.getId());
        Reservation reservation = reservationService.saveReservation(reservationDto);
        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity
                .created(URI.create("/reservations/" + response.getId()))
                .body(response);
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
        List<Reservation> responses = reservationService.searchReservationsByConditions(memberId, themeId, from, to);
        List<ReservationResponse> response = responses.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<OwnReservationResponse>> getReservationsOfMember(LoginMember member) {
        List<WaitingWithRank> waitingWithRank = waitingService.findWaitingByMember(member);
        List<Reservation> reservations = reservationService.findReservationsByMember(member);
        List<OwnReservationResponse> response = OwnReservationResponse.from(reservations, waitingWithRank);
        return ResponseEntity.ok(response);
    }
}
