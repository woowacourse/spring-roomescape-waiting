package roomescape.reservation.controller;

import io.restassured.RestAssured;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.repository.MemberRepository;
import roomescape.repository.fake.FakeMemberRepository;
import roomescape.repository.fake.FakeReservationTimeRepository;
import roomescape.repository.fake.FakeThemeRepository;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.repository.fake.FakeReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationControllerTest {
    @TestConfiguration
    static class TestConfig {
        @Bean
        public MemberRepository memberRepository() {
            return new FakeMemberRepository();
        }

        @Bean ReservationRepository reservationRepository() {
            return new FakeReservationRepository();
        }

        @Bean
        public ThemeRepository themeRepository() {
            return new FakeThemeRepository(reservationRepository());
        }

        @Bean
        public ReservationTimeRepository reservationTimeRepository() {
            return new FakeReservationTimeRepository(reservationRepository());
        }
    }

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthorizationProvider authorizationProvider;

    private AuthorizationPrincipal principal;

    @BeforeEach
    void setUp() {
        Theme theme = new Theme(null, "test theme", "test description", "test thumbnail");
        themeRepository.save(theme);

        ReservationTime reservationTime = new ReservationTime(null, LocalTime.now().plusHours(1));
        reservationTimeRepository.save(reservationTime);

        Member member = MemberFixture.createMember(MemberRole.USER);
        Member savedMember = memberRepository.save(member);

        principal = authorizationProvider.createPrincipal(AuthorizationPayload.fromMember(savedMember));
    }

    @Test
    void 예약_생성_확인() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now(), 1L, 1L);

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo(1));
    }

    @Test
    void 전체_예약_목록_불러오기() {
        // given
        createReservation();

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1));
    }

    @Test
    void 내_예약_목록_불러오기() {
        // given
        createReservation();

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .when().get("/reservations/my")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(1));
    }

    @Test
    void ID로_예약_삭제() {
        // given
        createReservation();

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        RestAssured.given().log().all()
                .contentType("application/json")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(0));

    }

    private void createReservation() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.now(), 1L, 1L);

        // when & then
        RestAssured.given().log().all()
                .contentType("application/json")
                .cookie("token", principal.value())
                .body(request)
                .when().post("/reservations");
    }
}
