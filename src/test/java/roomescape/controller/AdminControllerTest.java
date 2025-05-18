//package roomescape.controller;
//
//import static roomescape.test.fixture.DateFixture.NEXT_DAY;
//
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.HashMap;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.annotation.DirtiesContext.ClassMode;
//import org.springframework.transaction.annotation.Transactional;
//import roomescape.domain.Reservation;
//import roomescape.domain.ReservationStatus;
//import roomescape.domain.ReservationTime;
//import roomescape.domain.Role;
//import roomescape.domain.Theme;
//import roomescape.domain.User;
//import roomescape.dto.business.AccessTokenContent;
//import roomescape.dto.request.AdminReservationRequest;
//import roomescape.utility.JwtTokenProvider;
//
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@Transactional
//@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
//class AdminControllerTest {
//
//    @Autowired
//    private TestEntityManager entityManager;
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//
//    @DisplayName("어드민은 특정 멤버의 예약을 추가할 수 있다.")
//    @Test
//    void createReservationByAdmin() {
//        // given
//        User admin = entityManager.persist(
//                User.createWithoutId(Role.ROLE_ADMIN, "관리자", "admin@test.com", "qwer1234!"));
//        User user = entityManager.persist(
//                User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "qwer1234!"));
//        Theme theme = entityManager.persist(
//                Theme.createWithoutId("테마", "설명", "섬네일"));
//        ReservationTime time = entityManager.persist(
//                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
//
//        AdminReservationRequest request = new AdminReservationRequest(
//                user.getId(), theme.getId(), NEXT_DAY, time.getId());
//
//        String adminToken =
//                jwtTokenProvider.createToken(new AccessTokenContent(admin.getId(), admin.getRole(), admin.getName()));
//
//        // when & then
//        RestAssured.given().log().all()
//                .cookies("token", adminToken)
//                .contentType(ContentType.JSON)
//                .body(request)
//                .when().post("/admin/reservations")
//                .then().log().all()
//                .statusCode(HttpStatus.CREATED.value());
//    }
//
//    @DisplayName("필터를 통해 예약을 검색할 수 있다.")
//    @Test
//    void canSearchReservationByFilter() {
//        // given
//        User admin = entityManager.persist(
//                User.createWithoutId(Role.ROLE_ADMIN, "관리자", "admin@test.com", "qwer1234!"));
//        User user = entityManager.persist(
//                User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "qwer1234!"));
//        Theme theme = entityManager.persist(
//                Theme.createWithoutId("테마", "설명", "섬네일"));
//        ReservationTime time = entityManager.persist(
//                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
//        entityManager.persist(
//                Reservation.createWithoutId(LocalDate.now().plusDays(1), ReservationStatus.BOOKED, time, theme, user));
//        entityManager.persist(
//                Reservation.createWithoutId(LocalDate.now().plusDays(2), ReservationStatus.BOOKED, time, theme, user));
//        entityManager.persist(
//                Reservation.createWithoutId(LocalDate.now().plusDays(3), ReservationStatus.BOOKED, time, theme, user));
//
//        String adminToken =
//                jwtTokenProvider.createToken(new AccessTokenContent(admin.getId(), admin.getRole(), a));
//
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("userId", user.getId());
//        params.put("themeId", theme.getId());
//        params.put("from", LocalDate.now());
//        params.put("to", LocalDate.now().plusDays(3));
//
//        // when & then
//        RestAssured.given().log().all()
//                .cookies("token", adminToken)
//                .params(params)
//                .when().get("/admin/search")
//                .then().log().all()
//                .statusCode(HttpStatus.CREATED.value());
//    }
//}
