package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.BaseControllerTest;
import roomescape.controller.dto.request.ThemeRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.service.dto.response.ThemeResponse;

class ThemeControllerTest extends BaseControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @TestFactory
    @DisplayName("테마를 생성, 조회, 삭제한다.")
    Stream<DynamicTest> themeControllerTests() {
        return Stream.of(
                DynamicTest.dynamicTest("테마를 생성한다.", this::addTheme),
                DynamicTest.dynamicTest("테마를 모두 조회한다.", this::getAllThemes),
                DynamicTest.dynamicTest("테마를 삭제한다.", this::deleteThemeById)
        );
    }

    @TestFactory
    @DisplayName("중복된 이름의 테마를 생성하면 실패한다.")
    Stream<DynamicTest> failWhenDuplicatedTheme() {
        return Stream.of(
                DynamicTest.dynamicTest("테마를 생성한다.", this::addTheme),
                DynamicTest.dynamicTest("이미 존재하는 이름의 테마를 생성한다.", this::addThemeFailWhenDuplicatedTheme)
        );
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 실패한다.")
    void deleteThemeByIdFailWhenNotFoundId() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softly.assertThat(response.body().asString()).contains("해당 id의 테마가 존재하지 않습니다.");
        });
    }

    @Test
    @DisplayName("이미 사용 중인 테마을 삭제하면 실패한다.")
    void deleteThemeByIdFailWhenUsedTheme() {
        Member member = memberRepository.save(new Member("member@gmail.com", "password", "member", Role.USER));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));
        Theme theme = themeRepository.save(new Theme("테마 이름", "테마 설명", "https://example.com"));

        reservationRepository.save(new Reservation(LocalDate.of(2024, 6, 22), member, time, theme));

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/themes/" + theme.getId())
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softly.assertThat(response.body().asString()).contains("해당 테마를 사용하는 예약이 존재합니다.");
        });
    }

    @Test
    @DisplayName("인기있는 테마들을 조회한다.")
    @Sql("/popular-themes.sql")
    void getPopularThemes() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .param("startDate", "2024-04-06")
                .param("endDate", "2024-04-10")
                .param("limit", "3")
                .when().get("/themes/popular")
                .then().log().all()
                .extract();

        List<ThemeResponse> popularThemes = response.jsonPath()
                .getList("list", ThemeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(3);

            softly.assertThat(popularThemes.get(0).id()).isEqualTo(4);
            softly.assertThat(popularThemes.get(0).name()).isEqualTo("마법의 숲");
            softly.assertThat(popularThemes.get(0).description()).isEqualTo("요정과 마법사들이 사는 신비로운 숲 속으로!");
            softly.assertThat(popularThemes.get(0).thumbnail()).isEqualTo("https://via.placeholder.com/150/30f9e7");

            softly.assertThat(popularThemes.get(1).id()).isEqualTo(3);
            softly.assertThat(popularThemes.get(1).name()).isEqualTo("시간여행");
            softly.assertThat(popularThemes.get(1).description()).isEqualTo("과거와 미래를 오가며 역사의 비밀을 밝혀보세요.");
            softly.assertThat(popularThemes.get(1).thumbnail()).isEqualTo("https://via.placeholder.com/150/24f355");

            softly.assertThat(popularThemes.get(2).id()).isEqualTo(2);
            softly.assertThat(popularThemes.get(2).name()).isEqualTo("우주 탐험");
            softly.assertThat(popularThemes.get(2).description()).isEqualTo("끝없는 우주에 숨겨진 비밀을 파헤치세요.");
            softly.assertThat(popularThemes.get(2).thumbnail()).isEqualTo("https://via.placeholder.com/150/771796");
        });
    }

    private void addTheme() {
        ThemeRequest request = new ThemeRequest("테마 이름", "테마 설명", "https://example.com/image.jpg");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .extract();

        ThemeResponse themeResponse = response.as(ThemeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            softly.assertThat(response.header("Location")).isEqualTo("/themes/1");
            softly.assertThat(themeResponse)
                    .isEqualTo(new ThemeResponse(1L, "테마 이름", "테마 설명", "https://example.com/image.jpg"));
        });
    }

    private void addThemeFailWhenDuplicatedTheme() {
        ThemeRequest request = new ThemeRequest("테마 이름", "테마 설명-2", "https://example.com/image-2.jpg");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/themes")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            softly.assertThat(response.body().asString()).contains("해당 이름의 테마는 이미 존재합니다.");
        });

    }

    private void getAllThemes() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .extract();

        List<ThemeResponse> themeResponses = response.jsonPath()
                .getList("list", ThemeResponse.class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            softly.assertThat(themeResponses).hasSize(1);
            softly.assertThat(themeResponses)
                    .containsExactly(new ThemeResponse(1L, "테마 이름", "테마 설명", "https://example.com/image.jpg"));
        });
    }

    private void deleteThemeById() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .extract();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        });
    }
}
