package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.request.AdminReservationRequestDto;
import roomescape.dto.response.TokenResponseDto;
import roomescape.service.AuthService;
import roomescape.test.fixture.AuthFixture;
import roomescape.test.fixture.ReservationTimeFixture;
import roomescape.test.fixture.ThemeFixture;
import roomescape.test.fixture.UserFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminControllerTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthService authService;

    @LocalServerPort
    int port;

    private User savedMember;
    private User savedAdmin;
    private Theme savedTheme;
    private ReservationTime savedTime;
    private TokenResponseDto memberTokenResponseDto;
    private TokenResponseDto adminTokenResponseDto;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        date = LocalDate.now().plusDays(1);

        // Create and persist test data
        User member = UserFixture.create(Role.ROLE_MEMBER, "member_dummyName", "member_dummyEmail",
                "member_dummyPassword");
        User admin = UserFixture.create(Role.ROLE_ADMIN, "admin_dummyName", "admin_dummyEmail", "admin_dummyPassword");
        Theme theme = ThemeFixture.create("dummyName", "dummyDescription", "dummyThumbnail");
        ReservationTime time = ReservationTimeFixture.create(LocalTime.of(2, 40));

        savedMember = entityManager.persist(member);
        savedAdmin = entityManager.persist(admin);
        savedTheme = entityManager.persist(theme);
        savedTime = entityManager.persist(time);

        entityManager.flush();

        memberTokenResponseDto = authService.login(
                AuthFixture.createTokenRequestDto(savedMember.getEmail(), savedMember.getPassword()));
        adminTokenResponseDto = authService.login(
                AuthFixture.createTokenRequestDto(savedAdmin.getEmail(), savedAdmin.getPassword()));
    }

    @Nested
    @DisplayName("POST /admin/reservations 요청")
    class createReservation {

        @DisplayName("memberId의 role이 ROLE_MEMBER 일 때 201 CREATED 와 함께 member의 예약이 추가된다.")
        @Test
        void createReservation_success_byMemberId() {
            // given
            AdminReservationRequestDto dto = new AdminReservationRequestDto(date,
                    savedTheme.getId(),
                    savedTime.getId(),
                    savedMember.getId());

            String token = adminTokenResponseDto.accessToken();

            // when
            // then
            RestAssured.given().log().all()
                    .cookies("token", token)
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value());
        }

        @DisplayName("memberId에 role이 ROLE_ADMIN 일 때 401 A를 반환한다")
        @Test
        void createReservation_throwException_byAdminId() {
            // given
            AdminReservationRequestDto dto = new AdminReservationRequestDto(date,
                    savedTheme.getId(),
                    savedTime.getId(),
                    savedAdmin.getId());

            String token = adminTokenResponseDto.accessToken();

            // when
            // then
            RestAssured.given().log().all()
                    .cookies("token", token)
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
