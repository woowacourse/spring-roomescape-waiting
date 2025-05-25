package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.application.provider.JwtTokenProvider;
import roomescape.dto.response.WaitingAdminResponseDto;
import roomescape.infrastructure.db.MemberJpaRepository;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;
import roomescape.infrastructure.db.ThemeJpaRepository;
import roomescape.infrastructure.db.WaitingJpaRepository;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.model.Waiting;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingAdminAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    ThemeJpaRepository themeJpaRepository;

    @Autowired
    ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Autowired
    WaitingJpaRepository waitingJpaRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("관리자가 아닌 사용자가 접근하려고 하는 경우 401 을 반환한다")
    void test1() {
        // given
        Member user = memberJpaRepository.save(
                new Member("이름", "email@gamil.com", "password", Role.USER));

        // when
        RestAssured.port = port;
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtTokenProvider.createToken(user.getEmail()))
                .when().get("/admin/waitings")
                .then().assertThat().statusCode(401);
    }

    @Test
    @DisplayName("대기를 등록한 멤버와 관련 없이 모든 대기 목록을 조회할 수 있다")
    void test2() {
        String emailOfAdministrator = "email@gmail.com";
        Member firstOwner = memberJpaRepository.save(new Member("이름", emailOfAdministrator, "password", Role.ADMIN));
        Member secondOwner = memberJpaRepository.save(
                new Member("주인아님", "secondOwner@gmail.com", "password", Role.USER));

        Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
        ReservationTime reservationTime = reservationTimeJpaRepository.save(
                new ReservationTime(LocalTime.of(12, 30)));

        Waiting firstWaiting = waitingJpaRepository.save(new Waiting(
                LocalDateTime.now(),
                new Reservation(
                        LocalDate.now().plusDays(1),
                        reservationTime,
                        theme,
                        firstOwner,
                        LocalDate.now()
                )
        ));

        Waiting secondWaiting = waitingJpaRepository.save(new Waiting(
                LocalDateTime.now(),
                new Reservation(
                        LocalDate.now().plusDays(1),
                        reservationTime,
                        theme,
                        secondOwner,
                        LocalDate.now()
                )
        ));

        // when
        RestAssured.port = port;
        List<WaitingAdminResponseDto> actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtTokenProvider.createToken(emailOfAdministrator))
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", WaitingAdminResponseDto.class);

        // then
        List<Long> ids = actual.stream()
                .map(WaitingAdminResponseDto::id)
                .toList();

        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(ids).contains(firstWaiting.getId(), secondWaiting.getId())
        );
    }

    @Test
    @DisplayName("대기를 거절할 수 있다")
    void test3() {
        String emailOfAdministrator = "email@gmail.com";
        Member administrator = memberJpaRepository.save(new Member("이름", emailOfAdministrator, "password", Role.ADMIN));
        Member user = memberJpaRepository.save(
                new Member("사용자", "user@gmail.com", "password", Role.USER));

        Theme theme = themeJpaRepository.save(new Theme("새로운 테마", "새로운 설명", "썸네일"));
        ReservationTime reservationTime = reservationTimeJpaRepository.save(
                new ReservationTime(LocalTime.of(12, 30)));

        Waiting waiting = waitingJpaRepository.save(new Waiting(
                LocalDateTime.now(),
                new Reservation(
                        LocalDate.now().plusDays(1),
                        reservationTime,
                        theme,
                        user,
                        LocalDate.now()
                )
        ));

        // when
        RestAssured.port = port;
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", jwtTokenProvider.createToken(emailOfAdministrator))
                .when().delete("/admin/waitings/" + waiting.getId())
                .then().log().all()
                .statusCode(204);

        // then
        List<Waiting> waitings = waitingJpaRepository.findAll();

        assertThat(waitings).doesNotContain(waiting);
    }
}
