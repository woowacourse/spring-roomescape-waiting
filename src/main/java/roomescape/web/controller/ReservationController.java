package roomescape.web.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.service.ReservationService;
import roomescape.web.dto.request.member.MemberInfo;
import roomescape.web.dto.request.reservation.ReservationRequest;
import roomescape.web.dto.request.reservation.ReservationSearchCond;
import roomescape.web.dto.request.reservation.UserReservationRequest;
import roomescape.web.dto.response.reservation.ReservationResponse;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAllReservation() {
        List<ReservationResponse> response = reservationService.findAllReservation();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponse>> findAllReservationByConditions(
            @RequestParam("from") LocalDate start,
            @RequestParam("to") LocalDate end,
            @RequestParam("name") String memberName,
            @RequestParam("theme") String themeName
    ) {
        ReservationSearchCond searchCond = new ReservationSearchCond(start, end, memberName, themeName);
        List<ReservationResponse> reservations = reservationService.findAllReservationByConditions(
                searchCond);

        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> saveReservation(@Valid @RequestBody UserReservationRequest request,
                                                               MemberInfo memberInfo) {
        ReservationRequest reservationRequest = ReservationRequest.builder()
                .date(request.date())
                .memberId(memberInfo.id())
                .timeId(request.timeId())
                .themeId(request.themeId())
                .build();

        ReservationResponse response = reservationService.saveReservation(reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @DeleteMapping("/{reservation_id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable(value = "reservation_id") Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
