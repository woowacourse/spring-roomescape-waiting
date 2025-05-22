package roomescape.reservation.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.AvailableReservationTimeRequest;
import roomescape.reservation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateReservationResponse;
import roomescape.reservation.dto.CreateWaitingRequest;
import roomescape.reservation.dto.CreateWaitingResponse;
import roomescape.reservation.dto.ReservationMineResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationRestController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestBody final CreateReservationRequest createReservationRequest,
            final Member member
    ) {
        final Reservation savedReservation = reservationService.save(
                member,
                createReservationRequest.date(),
                createReservationRequest.timeId(),
                createReservationRequest.themeId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateReservationResponse.from(savedReservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable final Long id) {
        reservationService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CreateReservationResponse>> getReservations() {
        final List<Reservation> reservations = reservationService.findAll();
        final List<CreateReservationResponse> createReservationResponse = reservations.stream()
                .map(CreateReservationResponse::from)
                .toList();

        return ResponseEntity.ok(createReservationResponse);
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<AvailableReservationTimeResponse>> getAvailableReservationTimes(
            @ModelAttribute final AvailableReservationTimeRequest request) {

        return ResponseEntity.ok(
                reservationService.findAvailableReservationTimes(request.date(), request.themeId()).stream()
                        .map(availableReservationTime -> new AvailableReservationTimeResponse(
                                availableReservationTime.id(),
                                availableReservationTime.startAt(),
                                availableReservationTime.alreadyBooked()
                        ))
                        .toList()
        );
    }

    //TODO : 몇 번째 예약 대기인지,,,
    //TODO : 그러면, 서비스에서 총 몇개인지 파악을 해야함
    //그러면, waiting에 저장된 애들을 (테마 & 날짜 & 시간) 기준으로 일치하는 애들끼리
    //그 안에서 id를 기준으로 내림차순 정렬을 해서 member몇 번째에 있는지 파악해야함
    @GetMapping("/mine")
    public ResponseEntity<List<ReservationMineResponse>> getMyReservations(final Member member) {
        final List<ReservationMineResponse> reservationMineResponses = reservationService.findByMember(member)
                .stream()
                .map(ReservationMineResponse::from)
                .toList();
        final List<ReservationMineResponse> waitingMineResponses = reservationService.findWaitingByMember(member)
                .stream()
                .map(waiting -> {
                    final long rank = reservationService.getRankInWaiting(waiting);
                    return ReservationMineResponse.from(waiting, rank);
                })
                .toList();

        final List<ReservationMineResponse> combined = new ArrayList<>();
        combined.addAll(reservationMineResponses);
        combined.addAll(waitingMineResponses);

        return ResponseEntity.ok(combined);
    }

    @PostMapping("/waiting")
    public ResponseEntity<CreateWaitingResponse> createWaitingReservation(
            @RequestBody final CreateWaitingRequest createWaitingRequest,
            final Member member
    ) {
        final Waiting savedWaiting = reservationService.createWaitingReservation(
                member,
                createWaitingRequest.date(),
                createWaitingRequest.time(),
                createWaitingRequest.theme()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateWaitingResponse.from(savedWaiting));
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable final Long id) {
        reservationService.deleteWaitingById(id);

        return ResponseEntity.noContent().build();
    }
}
