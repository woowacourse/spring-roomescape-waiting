package roomescape.domain.reservation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.application.UserReservationThemeService;
import roomescape.domain.reservation.application.dto.response.ReservationThemeServiceResponse;
import roomescape.domain.reservation.controller.dto.response.ReservationThemeResponse;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class UserReservationThemeController {

    private final UserReservationThemeService userReservationThemeService;

    // TODO : Admin과 API를 공유하고 있다. 분리를 고민해보자.
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ReservationThemeResponse> getAll() {
        List<ReservationThemeServiceResponse> responses = userReservationThemeService.getAll();
        return responses.stream()
                .map(ReservationThemeResponse::from)
                .toList();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/populars")
    public List<ReservationThemeResponse> getPopularThemes(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<ReservationThemeServiceResponse> responses = userReservationThemeService.getPopularThemes(limit);
        return responses.stream()
                .map(ReservationThemeResponse::from)
                .toList();
    }
}
