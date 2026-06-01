package roomescape.reservationTime.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationTime.application.ReservationTimeService;
import roomescape.reservationTime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationTime.presentation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationTime.presentation.dto.response.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService service;

    public ReservationTimeController(ReservationTimeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> saveTime(
            @RequestBody @Valid ReservationTimeCreateRequest request
    ) {
        ReservationTimeCreateCommand createCommand = new ReservationTimeCreateCommand(request.startAt());
        ReservationTimeResponse response = ReservationTimeResponse.from(service.saveTime(createCommand));
        return ResponseEntity.created(URI.create("/times/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getTimes(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long themeId
    ) {
        Set<Long> bookedTimeIds = service.getBookedTimes(date, themeId);
        List<ReservationTimeResponse> responses = (service.getTimes()).stream()
                .map(time -> ReservationTimeResponse.from(time, bookedTimeIds.contains(time.getId())))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(
            @PathVariable Long id
    ) {
        service.deleteTime(id);
        return ResponseEntity.noContent().build();
    }
}
