package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.exception.ExceptionType.*;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.Fixture;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Name;
import roomescape.domain.Password;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.dto.ReservationResponse;
import roomescape.exception.ExceptionType;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.JwtGenerator;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
public class ReservationControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private JwtGenerator JWT_GENERATOR;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    private Theme defaultTheme1 = new Theme("theme1", "description", "thumbnail");
    private Theme defaultTheme2 = new Theme("theme2", "description", "thumbnail");

    private ReservationTime defaultTime = new ReservationTime(LocalTime.of(11, 30));
    private Member defaultMember = Fixture.defaultMember;
    private Member otherMember = new Member(
            new Name("otherName"),
            Role.USER,
            new Email("other@email.com"),
            new Password("otherPassword"));
    private Member admin = new Member(
            new Name("admin"),
            Role.ADMIN,
            new Email("admin@admin.com"),
            new Password("adminPassword")
    );
    private String token;
    private String othersToken;
    private String adminToken;

    @BeforeEach
    void initData() {
        RestAssured.port = port;

        defaultTheme1 = themeRepository.save(defaultTheme1);
        defaultTheme2 = themeRepository.save(defaultTheme2);
        defaultTime = reservationTimeRepository.save(defaultTime);
        defaultMember = memberRepository.save(defaultMember);
        otherMember = memberRepository.save(otherMember);
        admin = memberRepository.save(admin);
        token = generateTokenWith(defaultMember);
        othersToken = generateTokenWith(otherMember);
        adminToken = generateTokenWith(admin);
    }

    private String generateTokenWith(Member member) {
        return JWT_GENERATOR.generateWith(
                Map.of(
                        "id", member.getId(),
                        "name", member.getName().getValue(),
                        "role", member.getRole().getTokenValue()
                )
        );
    }

    @DisplayName("예약이 하나 존재할 때")
    @Nested
    class OneReservationTest {
        Member savedUser = defaultMember;
        Member notSaveUser = otherMember;

        Reservation savedReservation = new Reservation(
                LocalDate.now().plusDays(1),
                defaultTime,
                defaultTheme1,
                savedUser
        );

        @BeforeEach
        void saveReservation() {
            savedReservation = reservationRepository.save(savedReservation);
        }

        @DisplayName("다른 시간에 예약을 하나 생성할 수 있다.")
        @Test
        void createReservationTest() {
            Map<String, Object> reservationParam = Map.of(
                    "date", savedReservation.getDate().plusDays(1).toString(),
                    "timeId", savedReservation.getReservationTime().getId(),
                    "themeId", savedReservation.getTheme().getId());

            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(reservationParam)
                    .post("/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is((int) savedReservation.getId() + 1),
                            "member.name", is(savedUser.getName().getValue()),
                            "date", is(reservationParam.get("date")),
                            "time.startAt", is(savedReservation.getReservationTime().getStartAt().toString()),
                            "theme.name", is(savedReservation.getTheme().getName()));

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @DisplayName("예약이 이미 존재해도 예약을 추가로 생성할 수 있다. -> 예약 대기")
        @Test
        void createReservationWaitTest() {
            Map<String, Object> reservationParam = Map.of(
                    "date", savedReservation.getDate().toString(),
                    "timeId", savedReservation.getReservationTime().getId(),
                    "themeId", savedReservation.getTheme().getId());

            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(reservationParam)
                    .post("/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is((int) savedReservation.getId() + 1),
                            "member.name", is(defaultMember.getName().getValue()),
                            "date", is(reservationParam.get("date")),
                            "time.startAt", is(savedReservation.getReservationTime().getStartAt().toString()),
                            "theme.name", is(savedReservation.getTheme().getName()));

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @DisplayName("지난 시간에 예약을 생성할 수 없다.")
        @Test
        void createPastReservationTest() {
            Map<String, Object> reservationParam = Map.of(
                    "date", LocalDate.now().minusMonths(1).toString(),
                    "timeId", "1",
                    "themeId", "1");

            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(reservationParam)
                    .post("/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is(PAST_TIME_RESERVATION.getMessage()));
        }

        @DisplayName("본인 예약을 하나 삭제할 수 있다.")
        @Test
        void deleteReservationTest() {
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .delete("/reservations/" + savedReservation.getId())
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @DisplayName("본인의 예약 대기를 삭제할 수 있다.")
        @Test
        void deleteReservationWaitTest() {
            //given
            Map<String, Object> reservationParam = Map.of(
                    "date", savedReservation.getDate().toString(),
                    "timeId", savedReservation.getReservationTime().getId(),
                    "themeId", savedReservation.getTheme().getId());

            int waitingId = RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(reservationParam)
                    .post("/reservations")
                    .then().log().all()
                    .extract().jsonPath().get("id");

            //when & then
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .delete("/reservations/" + waitingId)
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("로그인 없이 예약 대기를 삭제할 수 없다.")
        @Test
        void deleteReservationWaitWithoutLoginFailTest() {
            RestAssured.given().log().all()
                    .when()
                    .delete("/reservations/" + savedReservation.getId())
                    .then().log().all()
                    .statusCode(401);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("다른 사람의 예약 대기를 삭제할 수 없다.")
        @Test
        void deleteOthersReservationWaitFailTest() {
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", othersToken)
                    .delete("/reservations/" + savedReservation.getId())
                    .then().log().all()
                    .statusCode(403);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("관리자는 다른 사람의 예약 대기를 삭제할 수 있다.")
        @Test
        void deleteWaitingByAdminTest() {
            //given
            Map<String, Object> reservationParam = Map.of(
                    "date", savedReservation.getDate().toString(),
                    "timeId", savedReservation.getReservationTime().getId(),
                    "themeId", savedReservation.getTheme().getId());

            int waitingId = RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(reservationParam)
                    .post("/reservations")
                    .then().log().all()
                    .extract().jsonPath().get("id");

            RestAssured.given().log().all()
                    .when()
                    .cookie("token", adminToken)
                    .delete("/admin/reservations/" + waitingId)
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("관리자도 다른 사람의 예약을 삭제할 수 없다.")
        @Test
        void deleteReservationByAdminFailTest() {
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", adminToken)
                    .delete("/admin/reservations/" + savedReservation.getId())
                    .then().log().all()
                    .statusCode(403)
                    .body("message", is(FORBIDDEN_DELETE.getMessage()));

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("일반 사용자는 관리자 권한 예약 삭제를 할 수 없다.")
        @Test
        void adminDeleteWaitingByUserFailTest() {
            //given
            Map<String, Object> reservationParam = Map.of(
                    "date", savedReservation.getDate().toString(),
                    "timeId", savedReservation.getReservationTime().getId(),
                    "themeId", savedReservation.getTheme().getId());

            int waitingId = RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(reservationParam)
                    .post("/reservations")
                    .then().log().all()
                    .extract().jsonPath().get("id");

            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .delete("/admin/reservations/" + waitingId)
                    .then().log().all()
                    .statusCode(403);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }
    }

    @DisplayName("예약이 10개 존재할 때")
    @Nested
    class ExistReservationTest {
        Reservation reservation1;
        Reservation reservation2;
        Reservation reservation3;
        Reservation reservation4;
        Reservation reservation5;
        Reservation reservation6;
        Reservation reservation7;
        Reservation reservation8;
        Reservation reservation9;
        Reservation reservation10;

        @BeforeEach
        void initData() {
            reservation1 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(5), defaultTime, defaultTheme1,
                            defaultMember));
            reservation2 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(4), defaultTime, defaultTheme1,
                            defaultMember));
            reservation3 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(3), defaultTime, defaultTheme1,
                            defaultMember));
            reservation4 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(2), defaultTime, defaultTheme1,
                            defaultMember));
            reservation5 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(1), defaultTime, defaultTheme1,
                            defaultMember));

            reservation6 = reservationRepository.save(
                    new Reservation(LocalDate.now(), defaultTime, defaultTheme2, defaultMember));
            reservation7 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(1), defaultTime, defaultTheme2,
                            defaultMember));
            reservation8 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(2), defaultTime, defaultTheme2,
                            defaultMember));
            reservation9 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(3), defaultTime, defaultTheme2,
                            defaultMember));
            reservation10 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(4), defaultTime, defaultTheme2,
                            defaultMember));

            System.out.println("defaultMember = " + defaultMember);
            System.out.println("otherMember = " + otherMember);
        }

        @DisplayName("존재하는 모든 예약을 조회할 수 있다.")
        @Test
        void getReservationTest() {
            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(10));
        }

        @DisplayName("자신의 모든 예약을 조회할 수 있다.")
        @Test
        void getMembersReservationTest() {
            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().get("/reservations/mine")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(10));
        }

        @DisplayName("날짜를 이용해서 검색할 수 있다.")
        @Test
        void searchWithDateTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .queryParams(Map.of(
                            "memberId", 1,
                            "themeId", 1,
                            "dateFrom", reservation3.getDate().toString(),
                            "dateTo", reservation7.getDate().toString()
                    ))
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation3),
                    ReservationResponse.from(reservation4),
                    ReservationResponse.from(reservation5)
            );
        }

        @DisplayName("날짜를 입력하지 않고 검색하면 자동으로 오늘의 날짜가 사용된다.")
        @Test
        void searchWithoutDateTest() {
            ReservationResponse[] reservationResponses = RestAssured.given()
                    .param("memberId", 1)
                    .param("themeId", 2).log().all()
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation6)
            );
        }

        @DisplayName("예약자 아이디를 사용하지 않으면 모든 예약자에 대해 조회한다.")
        @Test
        void searchWithoutMemberTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .params(Map.of("themeId", 1,
                            "dateFrom", reservation1.getDate().toString(),
                            "dateTo", reservation10.getDate().toString()
                    ))
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation1),
                    ReservationResponse.from(reservation2),
                    ReservationResponse.from(reservation3),
                    ReservationResponse.from(reservation4),
                    ReservationResponse.from(reservation5)
            );
        }

        @DisplayName("테마 아이디를 사용하지 않으면 모든 테마에 대해 조회한다.")
        @Test
        void searchWithoutThemeTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .params(Map.of(
                            "memberId", 1,
                            "dateFrom", reservation1.getDate().toString(),
                            "dateTo", reservation10.getDate().toString()
                    ))
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation1),
                    ReservationResponse.from(reservation2),
                    ReservationResponse.from(reservation3),
                    ReservationResponse.from(reservation4),
                    ReservationResponse.from(reservation5),
                    ReservationResponse.from(reservation6),
                    ReservationResponse.from(reservation7),
                    ReservationResponse.from(reservation8),
                    ReservationResponse.from(reservation9),
                    ReservationResponse.from(reservation10)
            );
        }

        @DisplayName("아무 값도 입력하지 않으면 오늘의 날짜로 모든 멤버, 테마에 대해 조회한다.")
        @Test
        void searchWithDateAndThemeTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation6)
            );
        }
    }
}
