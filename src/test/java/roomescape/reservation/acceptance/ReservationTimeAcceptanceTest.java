package roomescape.reservation.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.helper.TestHelper;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationTimeRequest.ReservationTimeCreateRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeAcceptanceTest {

    private static final String DEFAULT_EMAIL = "miso@email.com";
    private static final String DEFAULT_PASSWORD = "miso";
    private static final String DEFAULT_NAME = "미소";

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        Member member = new Member(0L, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD, RoleType.ADMIN);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void createTime() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var timeRequest = new ReservationTimeCreateRequest(LocalTime.of(10, 0));

        // when & then
        TestHelper.postWithToken("/times", timeRequest, token)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo(1))
                .body("startAt", equalTo("10:00"));
    }

    @Test
    @DisplayName("모든 예약 시간을 조회한다.")
    void getAllTimes() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var timeRequest = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        TestHelper.postWithToken("/times", timeRequest, token);

        // when & then
        TestHelper.get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].startAt", equalTo("10:00"));
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회한다.")
    void getAvailableTimes() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var timeRequest = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        TestHelper.postWithToken("/times", timeRequest, token);

        // when & then
        TestHelper.get("/times/available-times?date=2024-03-20&themeId=1")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].startAt", equalTo("10:00"))
                .body("[0].alreadyBooked", equalTo(false));
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void deleteTime() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var timeRequest = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        TestHelper.postWithToken("/times", timeRequest, token);

        // when & then
        TestHelper.deleteWithToken("/times/1", token)
                .then()
                .statusCode(HttpStatus.OK.value());

        TestHelper.get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 예외가 발생한다.")
    void deleteNonExistentTime() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // when & then
        TestHelper.deleteWithToken("/times/1", token)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body(equalTo("존재하지 않는 id 입니다."));
    }

    @Test
    @DisplayName("운영 시간 이외의 시간은 생성할 수 없다.")
    void createTimeOutsideOperatingHours() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var timeRequest = new ReservationTimeCreateRequest(LocalTime.of(9, 0));

        // when & then
        TestHelper.postWithToken("/times", timeRequest, token)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo("운영 시간 이외의 날짜는 예약할 수 없습니다."));
    }

    @Test
    @DisplayName("러닝 타임이 겹치는 시간은 생성할 수 없다.")
    void createTimeWithOverlappingRunningTime() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var timeRequest1 = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        var timeRequest2 = new ReservationTimeCreateRequest(LocalTime.of(11, 0));

        TestHelper.postWithToken("/times", timeRequest1, token);

        // when & then
        TestHelper.postWithToken("/times", timeRequest2, token)
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body(equalTo("러닝 타임이 겹치는 시간이 존재합니다."));
    }
}
