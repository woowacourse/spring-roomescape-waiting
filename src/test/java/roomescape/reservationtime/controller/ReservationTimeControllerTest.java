package roomescape.reservationtime.controller;

import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.member.controller.MemberController;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.controller.ReservationController;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;
import roomescape.reservationtime.service.ReservationTimeServiceFacade;
import roomescape.theme.controller.ThemeController;
import roomescape.theme.dto.request.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationTimeControllerTest {
    @Autowired
    private ReservationTimeController reservationTimeController;

    @Autowired
    private ReservationController reservationController;

    @Autowired
    private ThemeController themeController;

    @Autowired
    private MemberController memberController;

    @Autowired
    private ReservationTimeServiceFacade reservationTimeServiceFacade;

    @Test
    void 예약_시간_생성하기() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(16, 0));
        reservationTimeController.create(request);
        List<ReservationTimeResponse> reservationTimes = reservationTimeServiceFacade.findAll();
        assertThat(reservationTimes).hasSize(1);
    }

    @Test
    void 예약_시간_목록_불러오기() {
        List<ReservationTimeCreateRequest> createTimeRequests = List.of(
                new ReservationTimeCreateRequest(LocalTime.of(16, 0)),
                new ReservationTimeCreateRequest(LocalTime.of(17, 0)),
                new ReservationTimeCreateRequest(LocalTime.of(18, 0))
        );
        for (ReservationTimeCreateRequest createTimeRequest : createTimeRequests) {
            reservationTimeController.create(createTimeRequest);
        }
        List<ReservationTimeResponse> reservationTimes = reservationTimeServiceFacade.findAll();
        assertThat(reservationTimes).hasSize(3);
    }

    @Test
    void 예약_삭제_하기() {
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(LocalTime.of(16, 0));
        reservationTimeController.create(request);
        reservationTimeController.delete(1L);
        List<ReservationTimeResponse> reservationTimes = reservationTimeServiceFacade.findAll();
        assertThat(reservationTimes).hasSize(0);
    }

    @Test
    void 예약된_시간_목록_조회() {
        MemberPrincipal memberPrincipal = MemberPrincipal.fromMember(new Member("test", "test@test.com", MemberRole.USER, "1234"));
        reservationTimeController.create(new ReservationTimeCreateRequest(LocalTime.of(16, 0)));
        themeController.create(new ThemeCreateRequest("test theme", "test description", "test thumbnail"));
        memberController.signup(new MemberSignUpRequest("test", "test@test.com", "1234"));
        reservationController.create(new ReservationCreateRequest(LocalDate.now().plusDays(1), 1L, 1L), memberPrincipal);
        ResponseEntity<List<ReservationTimeResponseWithBookedStatus>> bookedReservationTimes = reservationTimeController.read(LocalDate.now().plusDays(1), 1L);
        List<ReservationTimeResponseWithBookedStatus> body = bookedReservationTimes.getBody();
        assertAll(
                () -> assertThat(bookedReservationTimes.getStatusCodeValue()).isEqualTo(200),
                () -> assertThat(bookedReservationTimes.getBody()).hasSize(1),
                () -> assertThat(body.getFirst().id()).isEqualTo(1L),
                () -> assertThat(body.getFirst().booked()).isTrue(),
                () -> assertThat(body.getFirst().startAt()).isEqualTo(LocalTime.of(16, 0))
        );
    }
}
