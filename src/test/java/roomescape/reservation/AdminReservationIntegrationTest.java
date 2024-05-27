package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
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
import roomescape.reservation.dto.response.FindAdminReservationResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.util.IntegrationTest;
import roomescape.waiting.repository.WaitingRepository;

@IntegrationTest
class AdminReservationIntegrationTest {
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    AdminReservationIntegrationTest(final MemberRepository memberRepository,
                                    final ReservationTimeRepository reservationTimeRepository,
                                    final ThemeRepository themeRepository,
                                    final ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    private String getTokenByLogin() {
        memberRepository.save(new Member("비밥", Role.ADMIN, "admin@naver.com", "hihi"));
        return RestAssured
                .given().log().all()
                .body(new LoginRequest("admin@naver.com", "hihi"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 성공")
    void createReservationByAdmin() {
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(201)
                .body("id", equalTo(1))
                .body("member.name", equalTo("몰리"))
                .body("theme.name", equalTo("테마이름"))
                .body("date", equalTo("2024-11-30"))
                .body("time.startAt", equalTo("20:00"));
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 실패: 예약 날짜 과거")
    void createReservationByAdmin_WhenDimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2000-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 날짜는 현재보다 과거일 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 실패: 예약 날짜 없음")
    void createReservationByAdmin_WhenDimeIsNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", null);
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 예약 날짜는 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("관리자 권한 예약 생성 실패: 회원 식별자 자연수 아님")
    void createReservationByAdmin_WhenMemberIdIsInvalidType(Long memberId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", memberId);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약하고자 하는 회원 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 실패: 회원 식별자 없음")
    void createReservationByAdmin_WhenMemberIdIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", null);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 회원은 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("관리자 권한 예약 생성 실패: 시간 식별자 자연수 아님")
    void createReservationByAdmin_WhenTimeIsInvalidType(Long timeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", timeId);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 시간 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 실패: 시간 식별자 없음")
    void createReservationByAdmin_WhenTimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", null);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 시간은 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("관리자 권한 예약 생성 실패: 테마 식별자 자연수 아님")
    void createReservationByAdmin_WhenThemeIdIsInvalidType(Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 실패: 테마 식별자 없음")
    void createReservationByAdmin_WhenThemeIdIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마는 필수입니다."));
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 실패: 없는 테마")
    void createReservationByAdmin_WhenThemeNotExist() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 테마가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 실패: 없는 시간")
    void createReservationByAdmin_WhenTimeNotExist() {
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 시간이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("관리자 권한 예약 생성 실패: 없는 회원")
    void createReservationByAdmin_WhenMemberNotExist() {
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 2);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 2에 해당하는 회원이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("관리자 방탈출 예약 목록 조회 성공")
    void getReservations() {
        // given
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));

        Reservation reservation1 = reservationRepository.save(
                new Reservation(memberRepository.getById(1L), LocalDate.parse("2024-11-23"),
                        reservationTimeRepository.getById(1L), themeRepository.getById(1L)));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(memberRepository.getById(1L), LocalDate.parse("2024-12-23"),
                        reservationTimeRepository.getById(1L), themeRepository.getById(1L)));

        // when
        List<FindAdminReservationResponse> findReservationResponses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .when().get("/admin/reservations")
                .then().log().all()

                .statusCode(200)
                .extract().jsonPath()
                .getList(".", FindAdminReservationResponse.class);

        // then
        assertThat(findReservationResponses).containsExactly(
                FindAdminReservationResponse.from(reservation1),
                FindAdminReservationResponse.from(reservation2)
        );
    }
}
