package roomescape.reservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
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
import roomescape.fixture.db.MemberDbFixture;
import roomescape.fixture.db.ReservationDateTimeDbFixture;
import roomescape.fixture.db.ThemeDbFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;

@Import(AuthServiceTestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReservationAdminApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CleanUp cleanUp;

    @Autowired
    private ReservationRepository reservationRepository;

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
    void 어드민은_전체_에약을_조회할_수_있다() {
        Member member1 = memberDbFixture.유저1_생성();
        Theme theme = themeDbFixture.공포();
        ReservationDateTime dateTime = reservationDateTimeDbFixture.내일_열시();

        reservationRepository.save(Reservation.reserve(member1, dateTime, theme));

        RestAssured.given().log().all()
                .cookie("token", StubTokenProvider.ADMIN_STUB_TOKEN)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("data.totalElements", is(1));
    }


    @Test
    void 방탈출_예약_페이지를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", StubTokenProvider.ADMIN_STUB_TOKEN)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 방탈출_예약_페이지를_삭제한다() {
        Member member1 = memberDbFixture.유저1_생성();
        Theme theme = themeDbFixture.공포();
        ReservationDateTime dateTime = reservationDateTimeDbFixture.내일_열시();

        Long id = reservationRepository.save(Reservation.reserve(member1, dateTime, theme)).getId();

        RestAssured.given().log().all()
                .cookie("token", StubTokenProvider.ADMIN_STUB_TOKEN)
                .when().delete("/admin/reservations/" + id)
                .then().log().all()
                .statusCode(204);
    }
}
