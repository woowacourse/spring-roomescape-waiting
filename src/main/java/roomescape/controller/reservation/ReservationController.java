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
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotTimes;
import roomescape.domain.reservation.Theme;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservation.AvailableTimeRequestDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.dto.reservation.ReservationTimeSlotResponseDto;
import roomescape.dto.reservation.ThemeResponseDto;
import roomescape.service.reservation.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private static final int THEME_RANKING_END_RANGE = 7;
    private static final int THEME_RANKING_START_RANGE = 1;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> reservations() {
        List<Reservation> reservations = reservationService.allReservations();
        List<ReservationResponseDto> reservationDtos = reservations.stream()
                .map((reservation) -> new ReservationResponseDto(reservation.getId(), reservation.getName(),
                        reservation.getStartAt(), reservation.getDate(), reservation.getThemeName()))
                .toList();
        return ResponseEntity.ok(reservationDtos);
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservations(
            @RequestBody @Valid AddReservationDto newReservationDto) {
        long addedReservationId = reservationService.addReservation(newReservationDto);
        Reservation reservation = reservationService.getReservationById(addedReservationId);

        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(reservation.getId(),
                reservation.getName(), reservation.getStartAt(), reservation.getDate(), reservation.getThemeName());
        return ResponseEntity.created(URI.create("/reservations/" + addedReservationId)).body(reservationResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservations(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<ReservationTimeSlotResponseDto>> availableReservationTimes(
            @Valid @ModelAttribute AvailableTimeRequestDto availableTimeRequestDto) {
        ReservationSlotTimes reservationSlotTimes = reservationService.availableReservationTimes(
                availableTimeRequestDto);
        List<ReservationSlot> availableBookTimes = reservationSlotTimes.getAvailableBookTimes();

        List<ReservationTimeSlotResponseDto> reservationTimeSlotResponseDtos = availableBookTimes.stream()
                .map((time) -> new ReservationTimeSlotResponseDto(time.getId(), time.getTime(), time.isReserved()))
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
}
