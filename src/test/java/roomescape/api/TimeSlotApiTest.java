package roomescape.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.response.TimeWithBookedResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TimeSlotApiTest {

    @Autowired
    private TimeSlotRepository timeSlotRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 예약시간_추가_테스트() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        // when
        ReservationTimeResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("api/times")
                .then().log().all()
                .statusCode(201)
                .extract().as(ReservationTimeResponse.class);

        // then
        List<TimeSlot> allTimeSlots = timeSlotRepository.findAll();
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response.startAt()).isEqualTo(LocalTime.of(10, 0));
        soft.assertThat(allTimeSlots).hasSize(1);
        soft.assertAll();
    }

    @Test
    void 예약시간_조회_테스트() {
        // given
        timeSlotRepository.save(TimeSlot.createWithoutId(LocalTime.of(10, 0)));
        // when
        List<ReservationTimeResponse> response = RestAssured.given().log().all()
                .when().get("/api/times")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", ReservationTimeResponse.class);
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response).hasSize(1);
        soft.assertThat(response.getFirst().startAt()).isEqualTo(LocalTime.of(10, 0));
        soft.assertAll();

    }

    @Test
    void 예약시간_삭제_테스트() {
        // given
        timeSlotRepository.save(TimeSlot.createWithoutId(LocalTime.of(10, 0)));
        // when
        RestAssured.given().log().all()
                .when().delete("/api/times/{timeId}", 1L)
                .then().log().all()
                .statusCode(204);

        // then
        List<TimeSlot> allTimeSlots = timeSlotRepository.findAll();
        assertThat(allTimeSlots).hasSize(0);
    }

    @Test
    void 가능한_예약시간_조회_테스트() {
        // when
        TimeSlot timeSlot1 = timeSlotRepository.save(
                TimeSlot.createWithoutId(LocalTime.of(10, 0))
        );
        TimeSlot timeSlot2 = timeSlotRepository.save(
                TimeSlot.createWithoutId(LocalTime.of(11, 0))
        );
        Theme theme = themeRepository.save(Theme.createWithoutId("theme1", "desc", "thumb"));
        Member member = memberRepository.save(
                new Member(null, "member1", "email1@domain.com", "password1", Role.MEMBER));
        reservationRepository.save(
                Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), timeSlot1, theme));
        // when
        List<TimeWithBookedResponse> response = RestAssured.given().log().all()
                .when().get("/api/times/theme/1?date=2025-01-01")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", TimeWithBookedResponse.class);
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(response).hasSize(2);
        soft.assertThat(response.get(0).alreadyBooked()).isTrue();
        soft.assertThat(response.get(1).alreadyBooked()).isFalse();
        soft.assertAll();
    }
}
