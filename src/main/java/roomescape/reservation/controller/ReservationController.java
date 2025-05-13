package roomescape.reservation.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import roomescape.common.utils.UriFactory;
import roomescape.member.auth.LoginMember;
import roomescape.member.auth.RoleRequired;
import roomescape.member.auth.vo.MemberInfo;
import roomescape.member.domain.Role;
import roomescape.reservation.controller.dto.AvailableReservationTimeWebResponse;
import roomescape.reservation.controller.dto.CreateReservationByAdminWebRequest;
import roomescape.reservation.controller.dto.CreateReservationWebRequest;
import roomescape.reservation.controller.dto.ReservationSearchWebRequest;
import roomescape.reservation.controller.dto.ReservationWebResponse;
import roomescape.reservation.service.ReservationService;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class ReservationController {

    public static final String BASE_PATH = "/reservations";

    private final ReservationService reservationService;

    @RoleRequired(value = Role.ADMIN)
    @GetMapping(BASE_PATH)
    public List<ReservationWebResponse> getAll() {
        return reservationService.getAll();
    }

    @GetMapping(BASE_PATH + "/mine")
    public List<ReservationWebResponse> getAll(@LoginMember MemberInfo memberInfo) {
        return reservationService.getAll(memberInfo.id());
    }

    @GetMapping(BASE_PATH + "/times")
    public List<AvailableReservationTimeWebResponse> getAvailable(
            @RequestParam final LocalDate date,
            @RequestParam final Long themeId) {
        return reservationService.getAvailable(date, themeId);
    }

    @PostMapping(BASE_PATH)
    public ResponseEntity<ReservationWebResponse> create(
            @RequestBody final CreateReservationWebRequest createReservationWebRequest,
            @LoginMember MemberInfo memberInfo) {
        final ReservationWebResponse reservationWebResponse = reservationService.create(createReservationWebRequest, memberInfo);
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(reservationWebResponse.id()));
        return ResponseEntity.created(location)
                .body(reservationWebResponse);
    }

    @PostMapping("/admin" + BASE_PATH)
    public ResponseEntity<ReservationWebResponse> createReservationByAdmin(
            @RequestBody final CreateReservationByAdminWebRequest createReservationByAdminWebRequest) {
        final ReservationWebResponse reservationWebResponse = reservationService.create(createReservationByAdminWebRequest);
        final URI location = UriFactory.buildPath(BASE_PATH, String.valueOf(reservationWebResponse.id()));
        return ResponseEntity.created(location)
                .body(reservationWebResponse);
    }

    @GetMapping("/admin" + BASE_PATH)
    public ResponseEntity<List<ReservationWebResponse>> getReservationsByAdmin(
            @ModelAttribute final ReservationSearchWebRequest reservationSearchWebRequest) {
        return ResponseEntity.ok(reservationService.search(reservationSearchWebRequest));
    }

    @DeleteMapping(BASE_PATH + "/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
