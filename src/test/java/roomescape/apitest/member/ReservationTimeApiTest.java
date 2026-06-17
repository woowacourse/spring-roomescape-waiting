package roomescape.apitest.member;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationTimeApiTest {
    private int initialReservationTimeSize;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        String sql = "SELECT COUNT(*) FROM reservation_time";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        initialReservationTimeSize = Optional.ofNullable(result).orElse(0);
    }

    @Test
    @DisplayName("사용자는 예약 시간을 조회할 수 있다.")
    void getReservationTime_Success() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(initialReservationTimeSize));
    }
}
