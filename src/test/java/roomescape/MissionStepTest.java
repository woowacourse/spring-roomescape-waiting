package roomescape;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.jwt.domain.TokenType;
import roomescape.auth.jwt.manager.JwtManager;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.ui.ReservationController;
import roomescape.reservation.ui.dto.CreateReservationWithUserIdWebRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationController reservationController;

    @Autowired
    private JwtManager jwtManager;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("1단계: localhost:8080/admin 요청 시 어드민 메인 페이지가 성공적으로 응답된다")
    void first() {
        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.ADMIN));

        final Claims claims = Jwts.claims()
                .add(User.Fields.id, user.getId().getValue())
                .add(User.Fields.name, user.getName().getValue())
                .add(User.Fields.role, user.getRole().name())
                .build();

        RestAssured.given().log().all()
                .cookie(TokenType.ACCESS.getDescription(), jwtManager.generate(claims, TokenType.ACCESS).getValue())
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("2단계: localhost:8080/admin/reservation 요청 시 예약 관리 페이지가 성공적으로 응답된다, " +
            "예약들을 조회할 수 있다")
    void second() {
        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.ADMIN));

        final Claims claims = Jwts.claims()
                .add(User.Fields.id, user.getId().getValue())
                .add(User.Fields.name, user.getName().getValue())
                .add(User.Fields.role, user.getRole().name())
                .build();

        RestAssured.given().log().all()
                .cookie(TokenType.ACCESS.getDescription(), jwtManager.generate(claims, TokenType.ACCESS).getValue())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .cookie(TokenType.ACCESS.getDescription(), jwtManager.generate(claims, TokenType.ACCESS).getValue())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("3단계: localhost:8080/reservations 에 POST 요청 시 예약이 추가되고, DELETE 요청 시 각각 예약이 취소된다")
    void third() {
        // given
        final ReservationTime time = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.now())
        );

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포 제목"),
                        ThemeDescription.from("공포 설명"),
                        ThemeThumbnail.from("gongpo.com/image/1")
                )
        );

        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.ADMIN));

        final CreateReservationWithUserIdWebRequest request = new CreateReservationWithUserIdWebRequest(
                LocalDate.now().plusDays(1),
                time.getId().getValue(),
                theme.getId().getValue(),
                user.getId().getValue()
        );

        final Claims claims = Jwts.claims()
                .add(User.Fields.id, user.getId().getValue())
                .add(User.Fields.name, user.getName().getValue())
                .add(User.Fields.role, user.getRole().name())
                .build();

        // when
        // then
        RestAssured.given().log().all()
                .cookie(TokenType.ACCESS.getDescription(), jwtManager.generate(claims, TokenType.ACCESS).getValue())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("user.id", is(user.getId().getValue().intValue()));

        RestAssured.given().log().all()
                .cookie(TokenType.ACCESS.getDescription(), jwtManager.generate(claims, TokenType.ACCESS).getValue())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie(TokenType.ACCESS.getDescription(), jwtManager.generate(claims, TokenType.ACCESS).getValue())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("JdbcTemplate로 DataSource객체에 접근할 수 있다" +
            "DataSource로 Connection 확인할 수 있다" +
            "Connection로 데이터베이스, 테이블 이름 검증할 수 있다")
    void fourth() {
        try (final Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATIONS", null).next()).isTrue();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("데이터베이스에 예약 하나 추가 후 예약 조회 API를 통해 조회한 예약 수와 데이터베이스 쿼리를 통해 조회한 예약 수가 같은지 비교할 수 있다")
    void fifth() {
        // given
        final ReservationTime time = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.now()));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포 제목"),
                        ThemeDescription.from("공포 설명"),
                        ThemeThumbnail.from("gongpo.com/image/1")
                ));

        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.ADMIN));

        final Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(1)),
                        time,
                        theme
                ));

        final Claims claims = Jwts.claims()
                .add(User.Fields.id, user.getId().getValue())
                .add(User.Fields.name, user.getName().getValue())
                .add(User.Fields.role, user.getRole().name())
                .build();

        // when
        // then

        final List<ReservationResponse> reservations = RestAssured.given().log().all()
                .cookie(TokenType.ACCESS.getDescription(), jwtManager.generate(claims, TokenType.ACCESS).getValue())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        final Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservations", Integer.class);

        assertThat(reservations.size()).isEqualTo(count);
    }

    @Test
    @DisplayName("예약 추가/삭제 API를 활용하고, 조회로 확인할 수 있다")
    void sixth() {
        // given
        final ReservationTime time = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.now()));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포 제목"),
                        ThemeDescription.from("공포 설명"),
                        ThemeThumbnail.from("gongpo.com/image/1")
                ));

        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));

        final CreateReservationWithUserIdWebRequest request = new CreateReservationWithUserIdWebRequest(
                LocalDate.now().plusDays(1),
                time.getId().getValue(),
                theme.getId().getValue(),
                user.getId().getValue()
        );

        final Claims claims = Jwts.claims()
                .add(User.Fields.id, user.getId().getValue())
                .add(User.Fields.name, user.getName().getValue())
                .add(User.Fields.role, user.getRole().name())
                .build();

        // when
        // then
        final ReservationResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(TokenType.ACCESS.getDescription(), jwtManager.generate(claims, TokenType.ACCESS).getValue())
                .body(request)
                .when().post(ReservationController.BASE_PATH)
                .then().log().all()
                .statusCode(201)
                .extract().as(ReservationResponse.class);

        final Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservations", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/reservations/%d".formatted(response.reservationId()))
                .then().log().all()
                .statusCode(204);

        final Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservations",
                Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    @DisplayName("시간으로 API를 관리할 수 있다")
    void seventh() {
        final ReservationTime time = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.now()));

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/times/%d".formatted(time.getId().getValue()))
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("컨트롤러에 jdbcTemplate가 존재하지 않는다")
    void ninth() {
        boolean isJdbcTemplateInjected = false;

        for (final Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
