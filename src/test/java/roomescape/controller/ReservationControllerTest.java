package roomescape.controller;

import static roomescape.TestFixture.ADMIN_LOGIN_REQUEST;
import static roomescape.TestFixture.ADMIN_ZEZE;
import static roomescape.TestFixture.DATE_AFTER_1DAY;
import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.MEMBER_LOGIN_REQUEST;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.ROOM_THEME1;
import static roomescape.TestFixture.VALID_STRING_DATE;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.request.ReservationCreateRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private RoomThemeRepository roomThemeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation reservation : reservations) {
            reservationRepository.deleteById(reservation.getId());
        }
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        for (ReservationTime reservationTime : reservationTimes) {
            reservationTimeRepository.deleteById(reservationTime.getId());
        }
        List<RoomTheme> roomThemes = roomThemeRepository.findAll();
        for (RoomTheme roomTheme : roomThemes) {
            roomThemeRepository.deleteById(roomTheme.getId());
        }
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            memberRepository.deleteById(member.getId());
        }
    }

    @DisplayName("모든 예약 내역 조회 성공 테스트")
    @Test
    void findAllReservations() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("멤버 예약 추가 성공 테스트")
    @Test
    void createMemberReservation() {
        // given
        memberRepository.save(MEMBER_BROWN);

        String accessToken = RestAssured
                .given().log().all()
                .body(MEMBER_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        Map<String, Object> reservationRequest = createReservationRequest(MEMBER_BROWN, VALID_STRING_DATE,
                RESERVATION_TIME_10AM, ROOM_THEME1);

        // then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("어드민 예약 추가 성공 테스트")
    @Test
    void createAdminReservation() {
        // given
        memberRepository.save(ADMIN_ZEZE);

        String accessToken = RestAssured
                .given().log().all()
                .body(ADMIN_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        Map<String, Object> reservationRequest = createReservationRequest(MEMBER_BROWN, VALID_STRING_DATE,
                RESERVATION_TIME_10AM, ROOM_THEME1);

        // then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/admin/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("내 예약 조회 성공 테스트")
    @Test
    void findMyReservations() {
        // given
        memberRepository.save(MEMBER_BROWN);

        String accessToken = RestAssured
                .given().log().all()
                .body(MEMBER_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        Map<String, Object> reservationRequest = createReservationRequest(MEMBER_BROWN, VALID_STRING_DATE,
                RESERVATION_TIME_10AM, ROOM_THEME1);

        // 예약 추가
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all();

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .when().get("/reservations-mine")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }


    @DisplayName("날짜 양식을 잘못 입력할 시 400을 응답한다.")
    @ParameterizedTest
    @ValueSource(strings = {"20223-10-11", "2024-13-1"})
    void invalidDateReservation(String value) {
        // given
        memberRepository.save(MEMBER_BROWN);

        String accessToken = RestAssured
                .given().log().all()
                .body(MEMBER_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        Map<String, Object> reservationRequest = createReservationRequest(MEMBER_BROWN, value, RESERVATION_TIME_10AM,
                ROOM_THEME1);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지나간 시간 예약 시도 시 400을 응답한다.")
    @Test
    void outdatedReservation() {
        // given
        memberRepository.save(MEMBER_BROWN);

        String accessToken = RestAssured
                .given().log().all()
                .body(MEMBER_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        Map<String, Object> reservationRequest = createReservationRequest(MEMBER_BROWN, "2023-12-12",
                RESERVATION_TIME_10AM, ROOM_THEME1);
        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("중복된 시간 예약 시도 시 400을 응답한다.")
    @Test
    void duplicateReservation() {
        // given
        memberRepository.save(MEMBER_BROWN);

        String accessToken = RestAssured
                .given().log().all()
                .body(MEMBER_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        Map<String, Object> reservationRequest = createReservationRequest(MEMBER_BROWN, VALID_STRING_DATE,
                RESERVATION_TIME_10AM, ROOM_THEME1);

        // 중복된 시간을 저장한다.
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all();

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(reservationRequest)
                .when().post("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("참조키가 존재하지 않음으로 인한 예약 추가 실패 테스트")
    @Test
    void noPrimaryKeyReservation() {
        // given
        memberRepository.save(ADMIN_ZEZE);

        String accessToken = RestAssured
                .given().log().all()
                .body(ADMIN_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(new ReservationCreateRequest(1L, DATE_AFTER_1DAY, 1L, 1L))
                .when().post("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("예약 취소 성공 테스트")
    @Test
    void deleteReservationSuccess() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        ReservationTime savedReservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        RoomTheme savedRoomTheme = roomThemeRepository.save(ROOM_THEME1);
        Reservation savedReservation = reservationRepository.save(
                new Reservation(member, DATE_AFTER_1DAY, savedReservationTime, savedRoomTheme));

        String accessToken = RestAssured
                .given().log().all()
                .body(MEMBER_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        // when & then
        Long id = savedReservation.getId();
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().delete("/reservations/" + id)
                .then().log().all().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("예약 취소 실패 테스트")
    @Test
    void deleteReservationFail() {
        // given
        long invalidId = 0;

        memberRepository.save(MEMBER_BROWN);
        String accessToken = RestAssured
                .given().log().all()
                .body(MEMBER_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().delete("/reservations/" + invalidId)
                .then().log().all().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    private Map<String, Object> createReservationRequest(Member member, String date, ReservationTime time,
                                                         RoomTheme theme) {
        Member savedMember = memberRepository.save(member);
        ReservationTime savedReservationTime = reservationTimeRepository.save(time);
        RoomTheme savedRoomTheme = roomThemeRepository.save(theme);

        return Map.of(
                "memberId", savedMember.getId(),
                "date", date,
                "timeId", savedReservationTime.getId(),
                "themeId", savedRoomTheme.getId());
    }
}
