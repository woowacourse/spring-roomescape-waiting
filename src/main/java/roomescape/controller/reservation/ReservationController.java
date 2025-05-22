package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.dto.reservationmember.MyReservationMemberResponseDto;
import roomescape.dto.reservationmember.ReservationMemberResponseDto;
import roomescape.dto.reservationtime.AvailableTimeRequestDto;
import roomescape.dto.reservationtime.ReservationTimeSlotResponseDto;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.infrastructure.auth.intercept.AuthenticationPrincipal;
import roomescape.infrastructure.auth.member.UserInfo;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reserveticket.ReserveTicketService;
import roomescape.service.waiting.WaitingService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final int THEME_RANKING_END_RANGE = 7;
    private static final int THEME_RANKING_START_RANGE = 1;

    private final ReservationService reservationService;
    private final ReserveTicketService reserveTicketService;
    private final WaitingService waitingService;

    public ReservationController(ReservationService reservationService,
                                 ReserveTicketService reserveTicketService,
                                 WaitingService waitingService) {
        this.reservationService = reservationService;
        this.reserveTicketService = reserveTicketService;
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationMemberResponseDto>> reservations(@RequestParam(required = false) Long themeId,
                                                                           @RequestParam(required = false) Long memberId,
                                                                           @RequestParam(required = false) LocalDate dateFrom,
                                                                           @RequestParam(required = false) LocalDate dateTo) {
        List<ReserveTicket> reserveTickets = null;
        boolean isFilterMode = true;
        if (themeId == null && memberId == null && dateFrom == null && dateTo == null) {
            isFilterMode = false;
            reserveTickets = reserveTicketService.allReservations();
        }

        if (isFilterMode) {
            reserveTickets = reserveTicketService.searchReservations(themeId, memberId, dateFrom, dateTo);
        }

        List<ReservationMemberResponseDto> reservationDtos = reserveTickets.stream()
                .map((reservationMember) -> new ReservationMemberResponseDto(reservationMember.getId(),
                        reservationMember.getName(),
                        reservationMember.getThemeName(),
                        reservationMember.getDate(),
                        reservationMember.getStartAt()))
                .toList();
        return ResponseEntity.ok(reservationDtos);
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservations(
            @RequestBody @Valid AddReservationDto newReservationDto,
            @AuthenticationPrincipal UserInfo userInfo) {
        long addedReservationId = reserveTicketService.addReservation(newReservationDto, userInfo.id());
        Reservation reservation = reservationService.getReservationById(addedReservationId);

        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(addedReservationId,
                reservation.getName(), reservation.getStartAt(), reservation.getDate(), reservation.getThemeName());
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
        ReservationSlotTimes reservationSlotTimes = reservationService.availableReservationTimes(
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
        List<Theme> rankingThemes = reservationService.getRankingThemes(LocalDate.now(), THEME_RANKING_START_RANGE,
                THEME_RANKING_END_RANGE);

        List<ThemeResponseDto> themeResponseDtos = rankingThemes.stream()
                .map((theme) -> new ThemeResponseDto(theme.getId(), theme.getDescription(),
                        theme.getName(), theme.getThumbnail()))
                .toList();
        return ResponseEntity.ok(themeResponseDtos);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationMemberResponseDto>> myReservations(
            @AuthenticationPrincipal UserInfo userInfo) {
        List<ReserveTicket> reserveTickets = reserveTicketService.memberReservations(userInfo.id());
        List<MyReservationMemberResponseDto> reservationDtos = reserveTickets.stream()
                .map(MyReservationMemberResponseDto::from)
                .collect(Collectors.toList());
        List<WaitingWithRank> waitingsWithRank = waitingService.getWaitingsWithRankByMemberId(userInfo.id());
        for (WaitingWithRank waitingWithRank : waitingsWithRank) {
            reservationDtos.add(MyReservationMemberResponseDto.from(waitingWithRank));
        }
        return ResponseEntity.ok(reservationDtos);
    }
}
