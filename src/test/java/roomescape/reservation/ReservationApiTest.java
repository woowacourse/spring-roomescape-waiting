package roomescape.reservation;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import roomescape.auth.stub.StubTokenProvider;
import roomescape.common.CleanUp;
import roomescape.config.AuthServiceTestConfig;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDateTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.reservation.domain.ReservationDateTime;

@Import(AuthServiceTestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CleanUp cleanUp;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        cleanUp.all();
    }

    @Test
    void 방탈출_예약을_생성한다() {
        Long memberId = memberDbFixture.유저1_생성().getId();
        Long themeId = themeDbFixture.공포().getId();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Long timeId = reservationDateTime.reservationTime().getId();
        String dateTime = formatDateTime(reservationDateTime.reservationDate().date());

        HashMap<String, Object> request = new HashMap<>();
        request.put("memberId", memberId);
        request.put("themeId", themeId);
        request.put("timeId", timeId);
        request.put("date", dateTime);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", StubTokenProvider.USER_STUB_TOKEN)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 예약_삭제시_존재하지_않는_예약이면_예외를_응답한다() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약_생성_시_null_값을_허용하지_않는다() {
        Long themeId = themeDbFixture.공포().getId();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Long timeId = reservationDateTime.reservationTime().getId();
        String dateTime = formatDateTime(reservationDateTime.reservationDate().date());

        HashMap<String, Object> request = new HashMap<>();
        request.put("memberId", null);
        request.put("themeId", themeId);
        request.put("timeId", timeId);
        request.put("date", dateTime);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", StubTokenProvider.USER_STUB_TOKEN)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    private String formatDateTime(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDate.format(formatter);
    }
}
