package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeAcceptanceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private ReservationTime savedReservationTime;

    @BeforeEach
    void setUp() {
        savedReservationTime = new ReservationTime(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("예약 시각 조회 시 저장된 예약 시각 내역을 모두 가져온다")
    void test1() {
        // given
        List<ReservationTimeResponseDto> reservationTimes = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationTimeResponseDto.class);

        // then
        assertThat(reservationTimes.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("정상적으로 예약이 등록되는 경우 201을 반환한다")
    void test2() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("startAt", LocalTime.of(20, 10).toString());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 존재하는 예약 시각을 이용해 등록하고자 한다면 409 를 반환한다.")
    void test3() {
        // given
        LocalTime savedTime = savedReservationTime.getStartAt();

        Map<String, String> params = new HashMap<>();
        params.put("startAt", savedTime.toString());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("특정 예약 시각을 삭제하는 경우 성공 시 204를 반환한다")
    void test7() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(savedReservationTime);
        Long savedId = reservationTime.getId();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/times/" + savedId)
                .then().log().all()
                .statusCode(204);
    }

}
