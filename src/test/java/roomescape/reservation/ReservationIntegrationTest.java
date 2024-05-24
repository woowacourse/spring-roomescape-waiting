package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

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
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.CreateReservationRequest;
import roomescape.reservation.dto.response.FindAvailableTimesResponse;
import roomescape.reservation.dto.response.FindReservationResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.util.IntegrationTest;
import roomescape.waiting.model.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@IntegrationTest
class ReservationIntegrationTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    private String getTokenByLogin(final Member member) {
        Member loginMember = memberRepository.save(member);
        return RestAssured
                .given().log().all()
                .body(new LoginRequest(loginMember.getEmail().getEmail(), loginMember.getPassword()))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    @Test
    @DisplayName("방탈출 예약 생성 성공")
    void createReservationTime() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));

        CreateReservationRequest createReservationRequest = new CreateReservationRequest(LocalDate.parse("2024-11-30"),
                1L, 1L);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(201);
    }

    @Test
    @DisplayName("방탈출 예약 생성 살패: 날짜 형식")
    void createReservationTime_WhenDimeIsInvalidType() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "asdf-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("date 필드의 형식이 잘못되었습니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 살패: 날짜 과거")
    void createReservationTime_WhenDimeIsPast() {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(LocalDate.parse("2000-11-30"),
                1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 날짜는 현재보다 과거일 수 없습니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 살패: 날짜 없음")
    void createReservationTime_WhenDimeIsNull() {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(null, 1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 예약 날짜는 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("방탈출 예약 생성 살패: 시간 식별자 형식")
    void createReservationTime_WhenTimeIsInvalidType(Long timeId) {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(LocalDate.parse("2024-11-30"),
                timeId, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 시간 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 살패: 시간 null")
    void createReservationTime_WhenTimeIsPast() {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(LocalDate.parse("2024-11-30"),
                null, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 시간은 필수입니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 살패: 시간 없음")
    void createReservation_WhenTimeNotExist() {
        themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(LocalDate.parse("2024-11-30"),
                1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 시간이 존재하지 않습니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("방탈출 예약 생성 살패: 테마 식별자 형식")
    void createReservationTime_WhenThemeIdIsInvalidType(Long themeId) {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(LocalDate.parse("2024-11-30"),
                1L, themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 테마 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 살패: 테마 null")
    void createReservation_WhenThemeIsNull() {
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(LocalDate.parse("2024-11-30"),
                1L, null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마는 필수입니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 살패: 테마 없음")
    void createReservation_WhenThemeNotExist() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));

        CreateReservationRequest createReservationRequest = new CreateReservationRequest(LocalDate.parse("2024-11-30"),
                1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 테마가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 살패: 중복 예약")
    void createReservation_WhenTimeAndDateAndThemeExist() {
        Member member = memberRepository.save(new Member("롸키", Role.USER, "loki@naver.com", "loki"));
        ReservationTime reservationTime = reservationTimeRepository.save(
                new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2025-12-23"), reservationTime, theme));

        CreateReservationRequest createReservationRequest = new CreateReservationRequest(reservation.getDate(),
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin(new Member("몰리", Role.USER, "login@naver.com", "hihi")))
                .body(createReservationRequest)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("이미 2025-12-23의 테마이름 테마에는 20:00 시의 예약이 존재하여 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("방탈출 예약 조회 성공")
    void getReservation() {
        Member member = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));

        Reservation reservation = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2024-11-23"), reservationTime, theme));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/" + reservation.getId())
                .then().log().all()

                .statusCode(200)
                .body("theme", equalTo(reservation.getTheme().getName()))
                .body("date", equalTo("2024-11-23"))
                .body("time", equalTo("20:00"));
    }

    @Test
    @DisplayName("방탈출 예약 조회 실패: 예약 없음")
    void getReservation_WhenTimeNotExist() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/1")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 예약이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("가능한 예약 시간 조회 성공")
    void getAvailableTimes() {
        Member member = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        ReservationTime reservationTime1 = reservationTimeRepository.save(
                new ReservationTime(LocalTime.parse("20:00")));
        ReservationTime reservationTime2 = reservationTimeRepository.save(
                new ReservationTime(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));

        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-11-23"), reservationTime1, theme));
        reservationRepository.save(new Reservation(member, LocalDate.parse("2024-12-23"), reservationTime1, theme));

        List<FindAvailableTimesResponse> findAvailableTimesResponses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/times?date=2024-11-23&themeId=1")
                .then().log().all()
                .extract().jsonPath()
                .getList(".", FindAvailableTimesResponse.class);

        assertAll(
                () -> assertThat(findAvailableTimesResponses).hasSize(2),
                () -> assertThat(findAvailableTimesResponses).containsExactlyInAnyOrder(
                        new FindAvailableTimesResponse(1L, LocalTime.parse("20:00"), true),
                        new FindAvailableTimesResponse(2L, LocalTime.parse("10:00"), false)
                )
        );
    }

    @Test
    @DisplayName("회원, 테마, 기간에 따른 검색 성공")
    void searchBy() {
        Member member = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        ReservationTime reservationTime1 = reservationTimeRepository.save(
                new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2024-11-23"), reservationTime1, theme));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2024-12-23"), reservationTime1, theme));
        Reservation reservation3 = reservationRepository.save(
                new Reservation(member, LocalDate.parse("2025-01-23"), reservationTime1, theme));

        List<FindReservationResponse> findReservationResponses = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/search?memberId=1&themeId=1&dateFrom=2024-12-23&dateTo=2025-01-23")
                .then().log().all()

                .statusCode(200)
                .extract().jsonPath()
                .getList(".", FindReservationResponse.class);

        assertThat(findReservationResponses).containsExactly(
                FindReservationResponse.from(reservation2),
                FindReservationResponse.from(reservation3)
        );
    }

    @Test
    @DisplayName("방탈출 예약 취소 성공: 대기가 없는 경우")
    void cancelReservation_WhenWaitingNotExists() {
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        Theme theme = themeRepository.save(ThemeFixture.getOne());
        Member reservationMember = memberRepository.save(MemberFixture.getOne("reservationMember@naver.com"));
        Reservation reservation = reservationRepository.save(
                new Reservation(reservationMember, LocalDate.parse("2024-11-23"), reservationTime, theme));

        String token = getTokenByLogin(new Member("파랑", Role.ADMIN, "admin@naver.com", "hihi"));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()

                .statusCode(204);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/reservations/1")
                .then().log().all()

                .statusCode(404);
    }

    @Test
    @DisplayName("방탈출 예약 취소 성공: 대기가 있는 경우")
    void cancelReservation_WhenWaitingExists() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("20:00")));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));
        Member reservationMember = memberRepository.save(MemberFixture.getOne("reservationMember@naver.com"));
        Member waitingMember = memberRepository.save(MemberFixture.getOne("mmmember@naver.com"));
        Reservation reservation = reservationRepository.save(
                new Reservation(reservationMember, LocalDate.parse("2024-11-23"), reservationTime, theme));
        waitingRepository.save(new Waiting(reservation, waitingMember));

        String token = getTokenByLogin(new Member("파랑", Role.ADMIN, "admin@naver.com", "hihi"));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/reservations/" + reservation.getId())
                .then().log().all()

                .statusCode(200);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/reservations/" + reservation.getId())
                .then().log().all()

                .statusCode(200);
    }
}
