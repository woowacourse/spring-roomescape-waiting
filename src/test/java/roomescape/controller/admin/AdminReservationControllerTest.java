package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.BaseControllerTest;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.service.dto.request.ReservationCreateRequest;

class AdminReservationControllerTest extends BaseControllerTest {

    @DisplayName("어드민이 예약을 추가한다.")
    @Test
    void createReservation() {
        // given
        ReservationCreateRequest request = createReservationRequest(TestFixture.getMember1(), TestFixture.TOMORROW,
                TestFixture.getReservationTime10AM(),
                TestFixture.getTheme1());

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .assertThat().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("멤버만을 이용하여 예약을 조회한다.")
    @ParameterizedTest(name = "멤버 ID={0}로 조회 시 {1}개의 예약이 조회된다.")
    @CsvSource(value = {"1/3", "2/4"}, delimiter = '/')
    @Sql("/test_search_data.sql")
    void searchByMember(String memberId, int expectedCount) {
        RestAssured.given().log().all()
                .param("themeId", "")
                .param("memberId", memberId)
                .param("dateFrom", "")
                .param("dateTo", "")
                .header("cookie", getAdminWithToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(expectedCount));
    }

    @DisplayName("테마만을 이용하여 예약을 조회한다.")
    @ParameterizedTest(name = "테마 ID={0}로 조회 시 {1}개의 예약이 조회된다.")
    @CsvSource(value = {"1/4", "2/3"}, delimiter = '/')
    @Sql("/test_search_data.sql")
    void searchByTheme(String themeId, int expectedCount) {
        RestAssured.given().log().all()
                .param("themeId", themeId)
                .param("memberId", "")
                .param("dateFrom", "")
                .param("dateTo", "")
                .header("cookie", getAdminWithToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(expectedCount));
    }

    @DisplayName("시작 날짜만을 이용하여 예약을 조회한다.")
    @ParameterizedTest(name = "오늘 날짜보다 {0}일 전인 날짜를 시작 날짜로 조회 시 {1}개의 예약이 조회된다.")
    @CsvSource(value = {"1/1", "7/7"}, delimiter = '/')
    @Sql("/test_search_data.sql")
    void searchByFromDate(int minusDays, int expectedCount) {
        RestAssured.given().log().all()
                .param("themeId", "")
                .param("memberId", "")
                .param("dateFrom", LocalDate.now().minusDays(minusDays).toString())
                .param("dateTo", "")
                .header("cookie", getAdminWithToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(expectedCount));
    }

    @DisplayName("종료 날짜만을 이용하여 예약을 조회한다..")
    @ParameterizedTest(name = "오늘 날짜보다 {0}일 전인 날짜를 종료 날짜로 조회 시 {1}개의 예약이 조회된다.")
    @CsvSource(value = {"1/7", "3/5", "7/1"}, delimiter = '/')
    @Sql("/test_search_data.sql")
    void searchByToDate(int minusDays, int expectedCount) {
        RestAssured.given().log().all()
                .param("themeId", "")
                .param("memberId", "")
                .param("dateFrom", "")
                .param("dateTo", LocalDate.now().minusDays(minusDays).toString())
                .header("cookie", getAdminWithToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(expectedCount));
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void deleteReservationSuccess() {
        // given
        Reservation saved = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.CONFIRMED);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().delete("/admin/reservations/" + saved.getId())
                .then().log().all()
                .assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("모든 대기 중인 예약을 조회한다.")
    @Test
    void findAllWaiting() {
        // given
        reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(), Status.WAITING);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .body("count", is(1));
    }

    @DisplayName("대기 중인 예약을 승인한다.")
    @Test
    void approveWaiting() {
        // given
        Reservation waiting = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.WAITING);

        // when
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().post("/admin/reservations/waiting/{id}/approve", waiting.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        // then
        reservationRepository.findById(waiting.getId())
                .ifPresent(reservation -> assertThat(reservation.getStatus()).isEqualTo(Status.CONFIRMED));
    }

    @DisplayName("대기 중인 예약을 거절한다.")
    @Test
    void denyWaiting() {
        // given
        Reservation waiting = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.WAITING);

        // when
        RestAssured.given().log().all()
                .header("cookie", getAdminWithToken())
                .when().post("/admin/reservations/waiting/{id}/deny", waiting.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        assertThat(reservationRepository.findById(waiting.getId())).isEmpty();
    }

    private Reservation reserveAfterSave(Member member, LocalDate date, ReservationTime time, Theme theme,
                                         Status status) {
        Member savedMember = memberRepository.save(member);
        ReservationTime savedReservationTime = timeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        return reservationRepository.save(new Reservation(savedMember, date, savedReservationTime, savedTheme, status));
    }

    private ReservationCreateRequest createReservationRequest(Member member, LocalDate date, ReservationTime time,
                                                              Theme theme) {
        Member savedMember = memberRepository.save(member);
        ReservationTime savedReservationTime = timeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        return new ReservationCreateRequest(savedMember.getId(), date, savedReservationTime.getId(),
                savedTheme.getId());
    }
}
