package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotTimes;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.dto.reservationmember.ReservationTicketResponseDto;
import roomescape.dto.reservationmember.ReservationTicketWaitingDto;
import roomescape.dto.reservationtime.AvailableTimeRequestDto;
import roomescape.dto.reservationtime.ReservationTimeSlotResponseDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.infrastructure.auth.intercept.AuthenticationPrincipal;
import roomescape.infrastructure.auth.member.UserInfo;
import roomescape.service.reserveticket.ReserveTicketService;
import roomescape.service.reserveticket.ReserveTicketWaiting;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final int THEME_RANKING_END_RANGE = 7;
    private static final int THEME_RANKING_START_RANGE = 1;

    private final ReserveTicketService reserveTicketService;

    public ReservationController(ReserveTicketService reserveTicketService) {
        this.reserveTicketService = reserveTicketService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTicketResponseDto>> reservations(@RequestParam(required = false) Long themeId,
                                                                           @RequestParam(required = false) Long memberId,
                                                                           @RequestParam(required = false) LocalDate dateFrom,
                                                                           @RequestParam(required = false) LocalDate dateTo) {
        List<ReserveTicketWaiting> reserveTickets = null;
        boolean isFilterMode = true;
        if (themeId == null && memberId == null && dateFrom == null && dateTo == null) {
            isFilterMode = false;
            reserveTickets = reserveTicketService.allReservationTickets();
        }

        if (isFilterMode) {
            reserveTickets = reserveTicketService.searchReservations(themeId, memberId, dateFrom, dateTo);
        }

        List<ReservationTicketResponseDto> reservationDtos = reserveTickets.stream()
                .filter(reserveTicketWaiting -> reserveTicketWaiting.getReservationStatus()
                        .equals(ReservationStatus.RESERVATION))
                .map((reservationTicketWaiting) -> new ReservationTicketResponseDto(reservationTicketWaiting.getId(),
                        reservationTicketWaiting.getName(),
                        reservationTicketWaiting.getThemeName(),
                        reservationTicketWaiting.getDate(),
                        reservationTicketWaiting.getStartAt()))
                .toList();
        return ResponseEntity.ok(reservationDtos);
    }


    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservations(
            @RequestBody @Valid AddReservationDto newReservationDto,
            @AuthenticationPrincipal UserInfo userInfo) {
        long addedReservationId = reserveTicketService.addReservation(newReservationDto, userInfo.id());
        Reservation reservation = reserveTicketService.getReservationById(addedReservationId);

        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(addedReservationId,
                reservation.getStartAt(), reservation.getDate(), reservation.getThemeName());
        return ResponseEntity.created(URI.create("/reservations/" + addedReservationId)).body(reservationResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservations(@PathVariable Long id) {
        reserveTicketService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<ReservationTimeSlotResponseDto>> availableReservationTimes(
            @Valid @ModelAttribute AvailableTimeRequestDto availableTimeRequestDto) {
        ReservationSlotTimes reservationSlotTimes = reserveTicketService.availableReservationTimes(
                availableTimeRequestDto);
        List<ReservationSlot> availableBookTimes = reservationSlotTimes.getAvailableBookTimes();

        List<ReservationTimeSlotResponseDto> reservationTimeSlotResponseDtos = availableBookTimes.stream()
                .map((time) -> new ReservationTimeSlotResponseDto(time.getReservationId(), time.getTime(),
                        time.isReserved()))
                .toList();
        return ResponseEntity.ok(reservationTimeSlotResponseDtos);
    }

    @GetMapping("/popular-themes")
    public ResponseEntity<List<ThemeResponseDto>> popularThemes() {
        List<Theme> rankingThemes = reserveTicketService.getRankingThemes(LocalDate.now(), THEME_RANKING_START_RANGE,
                THEME_RANKING_END_RANGE);

        List<ThemeResponseDto> themeResponseDtos = rankingThemes.stream()
                .map((theme) -> new ThemeResponseDto(theme.getId(), theme.getDescription(),
                        theme.getName(), theme.getThumbnail()))
                .toList();
        return ResponseEntity.ok(themeResponseDtos);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationTicketWaitingDto>> myReservations(
            @AuthenticationPrincipal UserInfo userInfo) {
        List<ReserveTicketWaiting> reserveTickets = reserveTicketService.memberReservationWaitingTickets(userInfo.id());
        List<ReservationTicketWaitingDto> reservationDtos = reserveTickets.stream()
                .map((reserveTicket) -> new ReservationTicketWaitingDto(reserveTicket.getId(),
                        reserveTicket.getName(),
                        reserveTicket.getThemeName(),
                        reserveTicket.getDate(),
                        reserveTicket.getStartAt(),
                        reserveTicket.getReservationStatus().getStatus(),
                        reserveTicket.getWaitRank()))
                .toList();
        return ResponseEntity.ok(reservationDtos);
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponseDto> addWaitingReservations(
            @RequestBody @Valid AddReservationDto newReservationDto,
            @AuthenticationPrincipal UserInfo userInfo) {
        long addedReservationId = reserveTicketService.addWaitingReservation(newReservationDto, userInfo.id());
        Reservation reservation = reserveTicketService.getReservationById(addedReservationId);

        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(addedReservationId,
                reservation.getStartAt(), reservation.getDate(), reservation.getThemeName());
        return ResponseEntity.created(URI.create("/reservations/" + addedReservationId)).body(reservationResponseDto);
    }

    @PostMapping("/waiting/{id}")
    public ResponseEntity<ReservationResponseDto> changeWaitingToReservation(@PathVariable Long id) {
        reserveTicketService.changeWaitingToReservation(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<ReservationResponseDto> removeWaitingReservations(@PathVariable Long id) {
        reserveTicketService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
