package roomescape.presentation;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.business.domain.Member;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.business.service.WaitingService;
import roomescape.infrastructure.repository.MemberRepository;
import roomescape.infrastructure.repository.ReservationTimeRepository;
import roomescape.infrastructure.repository.ThemeRepository;
import roomescape.presentation.dto.LoginRequest;
import roomescape.presentation.dto.WaitingRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("날짜, 시간, 테마에 대한 예약이 존재하지 않는 상태로 예약 대기를 할 시, 404 NOT FOUND가 반환되어야 한다")
    void createWaitingWithoutReservation() {
        //given
        Member member = new Member("이름", "USER", "이메일", "비밀번호");
        memberRepository.save(member);

        final LoginRequest loginRequest = new LoginRequest("이메일", "비밀번호");

        final String token = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/login")
                .getCookie("token");

        final ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));
        final Theme savedTheme = themeRepository.save(new Theme("이름", "설명", "썸네일"));

        final WaitingRequest waitingRequest = new WaitingRequest(LocalDate.now().plusDays(1), savedReservationTime.getId(),
                savedTheme.getId());

        //when & then
        given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(waitingRequest)
                .when()
                .post("/waitings")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(Matchers.equalTo("예약이 존재하지 않아 예약 대기를 할 수 없습니다."));
    }
}
