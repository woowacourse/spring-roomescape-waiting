package roomescape.member;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Slot;
import roomescape.reservation.model.Theme;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.util.IntegrationTest;

class AdminIntegrationTest extends IntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    private String getTokenByLogin() {
        memberRepository.save(new Member("비밥", Role.ADMIN, "admin@naver.com", "hihi"));
        return given().log().all()
                .body(new LoginRequest("admin@naver.com", "hihi"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }


    @Test
    @DisplayName("관리자 권한으로 예약을 생성한다.")
    void createReservationByAdmin() {
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        given().log().all()
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
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 날짜가 과거인 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenDimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2000-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 날짜는 현재보다 과거일 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 날짜가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenDimeIsNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", null);
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        given().log().all()
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
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 회원 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenMemberIdIsInvalidType(Long memberId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", memberId);
        params.put("timeId", 1);
        params.put("themeId", 1);

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약하고자 하는 회원 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 회원 식별자가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenMemberIdIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", null);
        params.put("timeId", 1);
        params.put("themeId", 1);

        given().log().all()
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
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 시간 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenTimeIsInvalidType(Long timeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", timeId);
        params.put("themeId", 1);

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 시간 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 시간 식별자가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenTimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", null);
        params.put("themeId", 1);

        given().log().all()
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
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 테마 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenThemeIdIsInvalidType(Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", themeId);

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 테마 식별자가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenThemeIdIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", null);

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마는 필수입니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시 해당하는 테마가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenThemeNotExist() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시 해당하는 시간이 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenTimeNotExist() {
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시 해당하는 시간이 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenMemberNotExist() {
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 2);
        params.put("timeId", 1);
        params.put("themeId", 1);

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("해당하는 사용자가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 대기를 예약으로 변경한다.")
    public void confirmWaitingSuccess() {
        Member member = memberRepository.save(MemberFixture.getOne());
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Waiting waiting = waitingRepository.save(new Waiting(member, new Slot(date, reservationTime, theme)));

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .when()
                .post("/admin/waitings/" + waiting.getId() + "/confirmation")
                .then()
                .statusCode(201)
                .header("Location", equalTo("/waitings/" + waiting.getId()))
                .body("id", equalTo(waiting.getId().intValue()));
    }

    @Test
    @DisplayName("관리자 권한으로 대기를 예약으로 변경 시, 이미 예약이 존재하는 경우 예외를 반환한다.")
    public void confirmWaitingFailDueToExistingReservation() {
        Member member = memberRepository.save(MemberFixture.getOne());
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Slot slot = new Slot(date, reservationTime, theme);
        reservationRepository.save(new Reservation(member, slot));
        Waiting waiting = waitingRepository.save(new Waiting(member, slot));

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .when()
                .post("/admin/waitings/" + waiting.getId() + "/confirmation")
                .then()
                .statusCode(400)
                .body("detail", equalTo("이미 예약이 존재하여 대기를 예약으로 변경할 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 대기를 예약으로 변경 시, 앞선 대기가 존재하는 경우 예외를 반환한다.")
    public void confirmWaitingFailDueToEarlierWaiting() {
        Member firstMember = memberRepository.save(MemberFixture.getOne());
        Member secondMember = memberRepository.save(MemberFixture.getOne("email@google.com"));
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Slot slot = new Slot(date, reservationTime, theme);
        Waiting first = waitingRepository.save(new Waiting(firstMember, slot));
        Waiting second = waitingRepository.save(new Waiting(secondMember, slot));

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .when()
                .post("/admin/waitings/" + second.getId() + "/confirmation")
                .then()
                .statusCode(400)
                .body("detail", equalTo(second.getId() + "번 예약 대기보다 앞선 대기가 존재하여 예약으로 변경할 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 대기를 예약으로 변경 시, 대기가 존재하지 않는 경우 예외를 반환한다.")
    public void confirmWaitingFailDueToNonExistentWaiting() {
        Long nonExistentId = 999L;

        given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .when()
                .post("/admin/waitings/" + nonExistentId + "/confirmation")
                .then()
                .statusCode(404)
                .body("detail", equalTo("식별자 " + nonExistentId + "에 해당하는 대기가 존재하지 않아 예약으로 변경할 수 없습니다."));
    }
}
