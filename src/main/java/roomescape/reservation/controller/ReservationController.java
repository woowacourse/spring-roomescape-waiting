package roomescape.reservation.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.utils.UriFactory;
import roomescape.member.auth.LoginMember;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.reservation.controller.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.controller.dto.CreateReservationWebRequest;
import roomescape.reservation.controller.dto.ReservationWaitWebResponse;
import roomescape.reservation.controller.dto.ReservationWebResponse;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;
import roomescape.reservation.service.ReservationService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/mine")
    public List<ReservationWithStatusResponse> getAllWithReservationWait(@LoginMember MemberInfo memberInfo) {
        return reservationService.getWithReservationWaitByMemberId(memberInfo.id());
    }

    @GetMapping("/times")
    public List<AvailableReservationTimeWebResponse> getAvailable(
            @RequestParam final LocalDate date,
            @RequestParam final Long themeId
    ) {
        return reservationService.getAvailable(date, themeId);
    }

    @PostMapping
    public ResponseEntity<ReservationWebResponse> create(
            @RequestBody final CreateReservationWebRequest createReservationWebRequest,
            @LoginMember MemberInfo memberInfo
    ) {
        final ReservationWebResponse reservationWebResponse = reservationService.create(
                createReservationWebRequest,
                memberInfo
        );
        final URI location = UriFactory.buildPath("/reservations", String.valueOf(reservationWebResponse.id()));
        return ResponseEntity.created(location)
                .body(reservationWebResponse);
    }

    @PostMapping("/wait")
    public ResponseEntity<ReservationWaitWebResponse> createReservationWait(
            @RequestBody final CreateReservationWebRequest createReservationWebRequest,
            @LoginMember MemberInfo memberInfo
    ) {
        final ReservationWaitWebResponse reservationWebResponse = reservationService.createReservationWait(
                createReservationWebRequest,
                memberInfo
        );
        final URI location = UriFactory.buildPath("/reservations", String.valueOf(reservationWebResponse.id()));

        return ResponseEntity.created(location)
                .body(reservationWebResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/wait" + "/{id}")
    public ResponseEntity<Void> deleteReservationWait(@PathVariable final Long id) {
        reservationService.deleteReservationWait(id);
        return ResponseEntity.noContent().build();
    }
}
