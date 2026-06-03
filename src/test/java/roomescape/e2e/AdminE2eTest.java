package roomescape.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.support.ApiFixtures.시간_등록;
import static roomescape.support.ApiFixtures.예약_대기_생성;
import static roomescape.support.ApiFixtures.예약_생성;
import static roomescape.support.ApiFixtures.테마_등록;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationEditableStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.time.dto.response.TimeResponseDto;
import roomescape.support.DatabaseCleaner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @Test
    void 삭제한_테마는_활성_목록에서_제외되고_관리자_목록에는_삭제_상태로_남는다() {
        // given
        ThemeResponseDto theme = 테마_등록("삭제될 테마", "설명", "https://example.com/image.png");

        // when
        given()
            .when().delete("/api/admin/themes/{id}", theme.id())
            .then().statusCode(204);

        // then
        List<ThemeResponseDto> activeThemes = given()
            .when().get("/api/themes")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(activeThemes).extracting(ThemeResponseDto::id).doesNotContain(theme.id());

        List<ThemeResponseDto> allThemes = given()
            .when().get("/api/admin/themes")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(allThemes)
            .filteredOn(found -> found.id().equals(theme.id()))
            .extracting(ThemeResponseDto::deleted)
            .containsExactly(true);
    }

    @Test
    void 삭제한_시간은_관리자_목록에_삭제_상태로_남는다() {
        // given
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));

        // when
        given()
            .when().delete("/api/admin/times/{id}", time.id())
            .then().statusCode(204);

        // then
        List<TimeResponseDto> allTimes = given()
            .when().get("/api/admin/times")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(allTimes)
            .filteredOn(found -> found.id().equals(time.id()))
            .extracting(TimeResponseDto::deleted)
            .containsExactly(true);
    }

    @Test
    void 관리자가_활성_예약을_삭제하면_가장_빠른_대기_예약이_자동_승격된다() {
        // given
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마", "설명", "https://example.com/image.png");
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationCreateResponseDto active = 예약_생성("예약자", date, time.id(), theme.id());
        ReservationCreateResponseDto waiting = 예약_대기_생성("대기자", date, time.id(), theme.id());

        // when
        given()
            .when().delete("/api/admin/reservations/{id}", active.id())
            .then().statusCode(204);

        // then
        List<ReservationResponseDto> 대기자_예약들 = given()
            .queryParam("name", "대기자")
            .when().get("/api/reservations")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(대기자_예약들)
            .filteredOn(found -> found.id().equals(waiting.id()))
            .extracting(ReservationResponseDto::status)
            .containsExactly(ReservationEditableStatus.EDITABLE);
    }
}
