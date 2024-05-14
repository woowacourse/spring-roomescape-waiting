package roomescape.presentation.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.ThemeRequest;
import roomescape.application.dto.ThemeResponse;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.MemberCommandRepository;
import roomescape.domain.Password;
import roomescape.domain.PlayerName;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationCommandRepository;
import roomescape.domain.ReservationQueryRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;

public class ThemeAcceptanceTest extends AcceptanceTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @DisplayName("테마를 추가한다.")
    @Test
    void createThemeTest() {
        ThemeRequest request = new ThemeRequest("이름", "설명", "url");

        ThemeResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .as(ThemeResponse.class);

        assertThat(response.name()).isEqualTo("이름");
        assertThat(response.description()).isEqualTo("설명");
        assertThat(response.thumbnail()).isEqualTo("url");
    }

    @DisplayName("인기 테마를 조회한다.")
    @Test
    void findPopularThemes() {
        Theme theme = themeRepository.create(ThemeFixture.defaultValue());
        ReservationTime reservationTime = reservationTimeRepository.create(ReservationTimeFixture.defaultValue());
        Member member = memberCommandRepository.create(new Member(new PlayerName("test"), new Email("test@email.com"), new Password("wootecoCrew6!"), Role.BASIC));
        Reservation reservation = reservationCommandRepository.create(
                new Reservation(member, LocalDate.now(), reservationTime, theme));

        RestAssured.given().log().all()
                .when()
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("테마가 존재해도 완료된 예약이 없으면 인기 테마도 없다.")
    @Test
    void notFoundPopularThemes() {
        themeRepository.create(ThemeFixture.defaultValue());

        RestAssured.given().log().all()
                .when()
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }
}
