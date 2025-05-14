package roomescape.domain.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.application.AdminReservationTimeService;
import roomescape.domain.reservation.application.dto.response.ReservationTimeServiceResponse;
import roomescape.domain.reservation.controller.dto.request.CreateReservationTimeRequest;
import roomescape.domain.reservation.controller.dto.response.ReservationTimeResponse;

@RestController
@RequestMapping("/admin/times")
@RequiredArgsConstructor
public class AdminReservationTimeController {

    private final AdminReservationTimeService adminReservationTimeService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ReservationTimeResponse create(@RequestBody @Valid CreateReservationTimeRequest request) {
        ReservationTimeServiceResponse response = adminReservationTimeService.create(request.toServiceRequest());
        return ReservationTimeResponse.from(response);
    }

    // TODO : User와 응답DTO 분리하기
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ReservationTimeResponse> getAll() {
        List<ReservationTimeServiceResponse> responses = adminReservationTimeService.getAll();
        return responses.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        adminReservationTimeService.delete(id);
    }
}
