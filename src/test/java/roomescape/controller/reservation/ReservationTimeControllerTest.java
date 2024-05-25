package roomescape.controller.reservation;

import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.TIME;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.domain.ReservationTime;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.request.ReservationTimeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationTimeControllerTest {

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
        clearTable();
    }

    @DisplayName("모든 예약 시간 조회 테스트")
    @Test
    void findAllReservationTime() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all().assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("예약 시간 추가 테스트")
    @Test
    void createReservationTime() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeRequest(TIME))
                .when().post("/times")
                .then().log().all().assertThat().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("시간 생성에서 잘못된 값 입력시 400을 응답한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "24:01", "12:60"})
    void invalidTypeReservationTime(String startAt) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("startAt", startAt))
                .when().post("/times")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("중복된 시간 추가 시도 시, 추가되지 않고 400을 응답한다.")
    @Test
    void duplicateReservationTime() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeRequest(reservationTime.getStartAt()))
                .when().post("/times")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("예약 시간 삭제 성공 테스트")
    @Test
    void deleteReservationTImeSuccess() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);

        // when & then
        Long id = reservationTime.getId();
        RestAssured.given().log().all()
                .when().delete("/times/" + id)
                .then().log().all().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("예약 시간 삭제 실패 테스트")
    @Test
    void deleteReservationTimeFail() {
        // given
        long invalidId = 0;

        // when & then
        RestAssured.given().log().all()
                .when().delete("/times/" + invalidId)
                .then().log().all().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    private void clearTable() {
        reservationRepository.findAll()
                .forEach(reservation -> reservationRepository.deleteById(reservation.getId()));
        reservationTimeRepository.findAll()
                .forEach(reservationTime -> reservationTimeRepository.deleteById(reservationTime.getId()));
        roomThemeRepository.findAll()
                .forEach(roomTheme -> roomThemeRepository.deleteById(roomTheme.getId()));
        memberRepository.findAll()
                .forEach(member -> memberRepository.deleteById(member.getId()));
    }
}
