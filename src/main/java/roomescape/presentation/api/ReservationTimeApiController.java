package roomescape.presentation.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthRequired;
import roomescape.auth.Role;
import roomescape.business.dto.ReservableReservationTimeDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.model.vo.UserRole;
import roomescape.business.service.ReservationTimeService;
import roomescape.presentation.dto.request.ReservationTimeRequest;
import roomescape.presentation.dto.response.BookedReservationTimeResponse;
import roomescape.presentation.dto.response.ReservationTimeResponse;

@RestController
@RequiredArgsConstructor
public class ReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    @PostMapping("/times")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @RequestBody @Valid ReservationTimeRequest request) {
        ReservationTimeDto dtos = reservationTimeService.addAndGet(request.startAtToLocalTime());
        ReservationTimeResponse response = ReservationTimeResponse.from(dtos);
        return ResponseEntity.created(URI.create("/times")).body(response);
    }

    @GetMapping("/times")
    @AuthRequired
    public ResponseEntity<List<ReservationTimeResponse>> getAllReservationTime() {
        List<ReservationTimeDto> reservationTimeDtos = reservationTimeService.getAll();
        List<ReservationTimeResponse> responses = ReservationTimeResponse.from(reservationTimeDtos);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/times/possible")
    @AuthRequired
    public ResponseEntity<List<BookedReservationTimeResponse>> getAvailableReservationTimes(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") String themeId
    ) {
        List<ReservableReservationTimeDto> reservationTimeDtos = reservationTimeService.getAllByDateAndThemeId(date,
                themeId);
        List<BookedReservationTimeResponse> responses = BookedReservationTimeResponse.from(reservationTimeDtos);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/times/{id}")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<Void> deleteReservationTime(@PathVariable String id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
