package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.AdminReservationService;
import roomescape.reservation.application.UserReservationService;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.controller.dto.request.AdminCreateReservationRequest;
import roomescape.reservation.controller.dto.request.ReservationSearchRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;
    private final UserReservationService userReservationService;

    //TODO : 현재 user의 예약 생성 로직 그대로 사용, 요구사항 변경되면 새로 어드민용 생성하기
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ReservationResponse create(@RequestBody @Valid AdminCreateReservationRequest request) {
        ReservationServiceResponse response = userReservationService.create(request.toServiceRequest());
        return ReservationResponse.from(response);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ReservationResponse> getAll() {
        List<ReservationServiceResponse> responses = adminReservationService.getAll();
        return responses.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search")
    public List<ReservationResponse> getSearchedAll(@ModelAttribute ReservationSearchRequest request) {
        List<ReservationServiceResponse> responses = adminReservationService.getSearchedAll(request.toServiceRequest());
        return responses.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        adminReservationService.delete(id);
    }
}
