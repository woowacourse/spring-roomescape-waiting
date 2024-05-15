package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.util.IntegrationTest;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;

@IntegrationTest
class ReservationIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @LocalServerPort
    private int port;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    private String getTokenByLogin() {
        memberRepository.save(new Member(null, "몰리", Role.USER, "login@naver.com", "hihi"));
        return RestAssured
                .given().log().all()
                .body(new LoginRequest("login@naver.com", "hihi"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    private void saveTimeThemeMemberForReservation() {
        memberRepository.save(new Member(null, "몰리", Role.USER, "login@naver.com", "hihi"));
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.parse("20:00")));
        themeRepository.save(new Theme(null, "테마이름", "설명", "썸네일"));
    }

    @Test
    @DisplayName("방탈출 예약 생성 성공 시, 생성된 시간대의 정보를 반환한다.")
    void createReservationTime() {
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.parse("20:00")));
        themeRepository.save(new Theme(null, "테마이름", "설명", "썸네일"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(201)
                .body("id", equalTo(1))
                .body("member.name", equalTo("몰리"))
                .body("theme.name", equalTo("테마이름"))
                .body("date", equalTo("2024-11-30"))
                .body("time.startAt", equalTo("20:00"));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 날짜가 형식에 맞지 않을 경우 예외를 반환한다.")
    void createReservationTime_WhenDimeIsInvalidType() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "asdf-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("입력값의 형식이 올바르지 않습니다. 다시 시도해주세요."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 날짜가 과거인 경우 예외를 반환한다.")
    void createReservationTime_WhenDimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2000-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 날짜는 현재보다 과거일 수 없습니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 날짜가 없는 경우 예외를 반환한다.")
    void createReservationTime_WhenDimeIsNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", null);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 예약 날짜는 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("방탈출 예약 생성 시, 시간 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationTime_WhenTimeIsInvalidType(Long timeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", timeId);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 시간 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 시간 식별자가 없는 경우 예외를 반환한다.")
    void createReservationTime_WhenTimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", null);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 시간은 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("방탈출 예약 생성 시, 테마 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationTime_WhenThemeIdIsInvalidType(Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 테마 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 테마 식별자가 없는 경우 예외를 반환한다.")
    void createReservationTime_WhenThemeIdIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마는 필수입니다."));
    }

    @Test
    @DisplayName("예약 생성 시 해당하는 테마가 없는 경우 예외를 반환한다.")
    void createReservation_WhenThemeNotExist() {
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.parse("20:00")));
        // themeRepository.save(new Theme(null, "테마이름", "설명", "썸네일"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("예약 생성 시 해당하는 시간이 없는 경우 예외를 반환한다.")
    void createReservation_WhenTimeNotExist() {
        // reservationTimeRepository.save(new ReservationTime(null, LocalTime.parse("20:00")));
        themeRepository.save(new Theme(null, "테마이름", "설명", "썸네일"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("예약 생성 시 이미 같은 테마, 같은 날짜, 같은 시간에 예약이 있는 경우 예외를 반환한다.")
    void createReservation_WhenTimeAndDateAndThemeExist() {
        Member member = memberRepository.save(new Member(null, "롸키", Role.USER, "loki@naver.com", "loki"));
        ReservationTime reservationTime = reservationTimeRepository.save(
                new ReservationTime(null, LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme(null, "테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(
                new Reservation(null, member, LocalDate.parse("2025-12-23"), reservationTime, theme));

        Map<String, Object> params = new HashMap<>();
        params.put("date", reservation.getDate());
        params.put("timeId", reservation.getReservationTime().getId());
        params.put("themeId", reservation.getTheme().getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("이미 2025-12-23의 테마이름 테마에는 20:00 시의 예약이 존재하여 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("방탈출 예약 목록을 조회한다.")
    void getReservationTimes() {
        saveTimeThemeMemberForReservation();
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-11-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-12-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()

                .statusCode(200)
                .body("id", hasItems(1, 2))
                .body("size()", equalTo(2));
    }

    @Test
    @DisplayName("방탈출 예약 하나를 조회한다.")
    void getReservationTime() {
        saveTimeThemeMemberForReservation();
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-11-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-12-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/1")
                .then().log().all()

                .statusCode(200)
                .body("id", equalTo(1))
                .body("member.name", equalTo("몰리"))
                .body("date", equalTo("2024-11-23"));
    }

    @Test
    @DisplayName("방탈출 예약 조회 시, 조회하려는 예약이 없는 경우 예외를 반환한다.")
    void getReservationTime_WhenTimeNotExist() {
        // reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-12-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/1")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 예약이 존재하지 않아 예약을 조회할 수 없습니다."));
    }

    @Test
    @DisplayName("해당 날짜와 테마를 통해 예약 가능한 시간 조회한다.")
    void getAvailableTimes() {
        saveTimeThemeMemberForReservation();
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.parse("10:00")));
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-11-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-12-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/times?date=2024-11-23&themeId=1")
                .then().log().all()

                .statusCode(200)
                .body("[0].id", equalTo(1))
                .body("[0].alreadyBooked", equalTo(true))

                .body("[1].id", equalTo(2))
                .body("[1].alreadyBooked", equalTo(false))
                .body("size()", equalTo(2));
    }

    @Test
    @DisplayName("해당 날짜와 테마, 기간에 해당하는 예약을 검색한다.")
    void searchBy() {
        saveTimeThemeMemberForReservation();
        reservationTimeRepository.save(new ReservationTime(null, LocalTime.parse("10:00")));
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-11-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-12-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2025-01-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/search?memberId=1&themeId=1&dateFrom=2024-12-23&dateTo=2025-01-23")
                .then().log().all()

                .statusCode(200)
                .body("[0].id", equalTo(2))
                .body("[0].date", equalTo("2024-12-23"))

                .body("[1].id", equalTo(3))
                .body("[1].date", equalTo("2025-01-23"));
    }

    @Test
    @DisplayName("방탈출 예약 하나를 삭제한다.")
    void deleteReservationTime() {
        saveTimeThemeMemberForReservation();
        reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-11-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()

                .statusCode(204);
    }

    @Test
    @DisplayName("방탈출 예약 조회 시, 조회하려는 예약이 없는 경우 예외를 반환한다.")
    void deleteReservationTime_WhenTimeNotExist() {
        // reservationRepository.save(new Reservation(null, memberRepository.getById(1L), LocalDate.parse("2024-11-23"), reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 예약이 존재하지 않습니다. 삭제가 불가능합니다."));
    }
}
