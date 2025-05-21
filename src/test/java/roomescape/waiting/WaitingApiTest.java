package roomescape.waiting;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import roomescape.auth.infrastructure.jwt.JwtProvider;
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
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Import(AuthServiceTestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WaitingApiTest {

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
    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        cleanUp.all();
    }

    @Test
    void 대기_예약을_생성한다() {
        Member 유저1 = memberDbFixture.유저1_생성();
        String token = jwtProvider.issue(유저1);

        Member 유저2 = memberDbFixture.유저2_생성();
        Theme 공포 = themeDbFixture.공포();
        Long themeId = 공포.getId();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Long timeId = reservationDateTime.getReservationTime().getId();
        LocalDate date = reservationDateTime.getDate();

        // 예약이 이미 존재해야 대기 등록 가능
        reservationRepository.save(Reservation.builder()
                .theme(공포)
                .reserver(유저2)
                .reservationDateTime(reservationDateTime)
                .build());

        HashMap<String, Object> request = new HashMap<>();
        request.put("themeId", themeId);
        request.put("timeId", timeId);
        request.put("date", date.toString());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 본인_대기_예약을_삭제한다() {
        Member 유저1 = memberDbFixture.유저1_생성();
        Member 유저2 = memberDbFixture.유저2_생성();

        Theme 공포 = themeDbFixture.공포();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        String token = jwtProvider.issue(유저1);

        // 예약이 이미 존재해야 대기 등록 가능
        reservationRepository.save(Reservation.builder()
                .theme(공포)
                .reserver(유저2)
                .reservationDateTime(reservationDateTime)
                .build());

        // 대기 신청
        Long id = waitingRepository.save(Waiting.builder()
                .theme(공포)
                .reserver(유저1)
                .reservationDateTime(reservationDateTime)
                .build()).getId();

        // 본인 대기 삭제
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/waiting/" + id)
                .then().log().all()
                .statusCode(204);
    }
}
