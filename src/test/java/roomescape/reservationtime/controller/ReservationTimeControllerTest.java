package roomescape.reservationtime.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.reservationtime.domain.dto.ReservationTimeRequestDto;
import roomescape.reservationtime.domain.dto.ReservationTimeResponseDto;
import roomescape.reservationtime.fixture.ReservationTimeFixture;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {
        ReservationTimeTestDataConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationTimeControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ReservationTimeTestDataConfig reservationTimeTestDataConfig;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @LocalServerPort
    int port;

    @BeforeEach
    void restAssuredSetUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("GET /times 요청")
    class findAll {

        @DisplayName("데이터가 있을 때 200 OK와 함께 예약 시간을 반환한다")
        @Test
        void findAll_success_whenDataExists() {
            // given 
            reservationTimeRepository.save(ReservationTimeFixture.create(LocalTime.of(15, 40)));

            // when & then
            List<ReservationTimeResponseDto> resDtos = RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value()).extract()
                    .jsonPath().getList(".", ReservationTimeResponseDto.class);

            Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation_time", Integer.class);
            assertThat(resDtos.size()).isEqualTo(count);
        }

        @DisplayName("데이터가 없을 때도 200 OK와 빈 리스트를 반환한다")
        @Test
        void findAll_success_whenNoData() {
            // when & then
            List<ReservationTimeResponseDto> resDtos = RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value()).extract()
                    .jsonPath().getList(".", ReservationTimeResponseDto.class);

            Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation_time", Integer.class);
            assertThat(resDtos.size()).isEqualTo(count);
        }
    }

    @Nested
    @DisplayName("POST /times 요청")
    class add {

        @DisplayName("유효한 입력 시 201 Created와 함께 예약 시간이 추가된다")
        @Test
        void add_success_whenValidInput() {
            // given
            LocalTime dummyTime = LocalTime.of(18, 22);

            // when
            ReservationTimeRequestDto dto = new ReservationTimeRequestDto(dummyTime);

            // then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value());
        }

        @DisplayName("중복된 예약 시간 입력 시 409 Conflict를 반환한다")
        @Test
        void add_failure_whenDuplicateInput() {
            // given
            LocalTime dummyTime = LocalTime.of(11, 22);
            reservationTimeRepository.save(ReservationTimeFixture.create(dummyTime));

            // when
            ReservationTimeRequestDto dto = new ReservationTimeRequestDto(dummyTime);

            // then
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(HttpStatus.CONFLICT.value());
        }
    }

    @Nested
    @DisplayName("DELETE /times/{id} 요청")
    class deleteById {

        @DisplayName("존재하는 ID로 삭제 요청 시 204 No Content를 반환한다")
        @Test
        void deleteById_success_withExistId() {
            RestAssured.given().log().all()
                    .when().delete("/times/" + reservationTimeTestDataConfig.getSavedId())
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            Integer countAfterDelete = jdbcTemplate.queryForObject(
                    "SELECT count(1) from reservation_time WHERE id = " + reservationTimeTestDataConfig.getSavedId(),
                    Integer.class);
            assertThat(countAfterDelete).isEqualTo(0);
        }

        @DisplayName("존재하지 않는 ID로 삭제 요청 시 400 Bad Request를 반환한다")
        @Test
        void deleteById_failure_byNonExistenceId() {
            Long maxValue = Long.MAX_VALUE;
            RestAssured.given().log().all()
                    .when().delete("/times/" + maxValue)
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
