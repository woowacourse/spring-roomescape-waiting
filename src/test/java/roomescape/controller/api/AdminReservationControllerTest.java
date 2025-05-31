package roomescape.controller.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.dto.reservation.AdminReservationCreateRequestDto;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.util.JwtTokenProvider;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/test-data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminReservationControllerTest {

    @Autowired
    JpaMemberRepository memberRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    String loginToken;

    @BeforeEach
    void setUp() {
        Member admin = new Member(null, "moda", "moda@woowa.com", Role.ADMIN, "password");
        memberRepository.save(admin);

        loginToken = jwtTokenProvider.createToken(new Member(2L, "moda", "moda@woowa.com", Role.ADMIN, "password"));
    }

    @Nested
    class AdminAddReservationTest {

        @DisplayName("어드민 예약 추가 테스트")
        @Test
        void addReservationTest() {
            AdminReservationCreateRequestDto dto = new AdminReservationCreateRequestDto(
                    LocalDate.now().plusDays(1), 1L, 1L, 1L);
            RestAssured.given().cookie("token", loginToken).log().all()
                    .contentType(ContentType.JSON)
                    .body(dto)
                    .when().post("/admin/reservations")
                    .then().log().all().statusCode(201);
        }
    }

    @Nested
    class searchAdminReservationTest {

        @Autowired
        JpaReservationRepository reservationRepository;

        @BeforeEach
        void setUp() {
            Reservation reservationInPast = new Reservation(null,
                    new Member(1L, "moda", "moda@woowa.com", Role.ADMIN, "password"),
                    LocalDate.of(2024, 12, 31),
                    new ReservationTime(1L, LocalTime.of(10, 0)),
                    new Theme(1L, "테마 A", "테마 A입니다.",
                            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"),
                    ReservationStatus.RESERVED
            );
            reservationRepository.save(reservationInPast);
        }

        @DisplayName("특정 기간 내 예약을 조회할 수 있다")
        @Test
        void searchAdminReservationTest() {
            Map<String, Object> params = Map.of(
                    "themeId", 1L,
                    "memberId", 1L,
                    "dateFrom", "2025-05-01",
                    "dateTo", "2025-12-31");

            RestAssured.given().log().all()
                    .cookie("token", loginToken)
                    .queryParams(params)
                    .when().get("/admin/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }
    }
}
