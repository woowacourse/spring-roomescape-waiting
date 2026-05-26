package roomescape.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Theme;
import roomescape.domain.vo.Name;
import roomescape.dto.response.AvailableTimeResponseDto;
import roomescape.dto.response.TimeResponseDto;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.dao.MemberDao;
import roomescape.service.ThemeService;

@WebMvcTest(ThemeController.class)
class ThemeControllerTest {

    private final Theme theme = new Theme(1L, new Name("방탈출테마"), "http://example.com/img.jpg", "방탈출 테마 설명");

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ThemeService themeService;
    @MockitoBean
    private MemberDao memberDao;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Nested
    class Get {

        @Test
        @DisplayName("전체 테마 목록을 조회하면 200을 반환한다")
        void returnsAllThemes() {
            given(themeService.findAll()).willReturn(List.of(theme));
            List<ThemeResponseDto> expected = List.of(ThemeResponseDto.from(theme));

            List<ThemeResponseDto> actual = RestAssuredMockMvc.given()
                    .when().get("/themes")
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(new TypeRef<>() {});

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("존재하는 테마 id를 조회하면 200을 반환한다")
        void returnsThemeById() {
            given(themeService.findById(theme.getId())).willReturn(theme);
            ThemeResponseDto expected = ThemeResponseDto.from(theme);

            ThemeResponseDto actual = RestAssuredMockMvc.given()
                    .when().get("/themes/" + theme.getId())
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(ThemeResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("테마의 이용 가능한 시간 목록을 조회하면 200을 반환한다")
        void returnsAvailableTimes() {
            List<AvailableTimeResponseDto> expected = List.of(
                    new AvailableTimeResponseDto(new TimeResponseDto(1L, LocalTime.of(13, 0)), false),
                    new AvailableTimeResponseDto(new TimeResponseDto(2L, LocalTime.of(14, 0)), true)
            );
            given(themeService.findAvailableTimesById(eq(theme.getId()), any(LocalDate.class))).willReturn(expected);

            List<AvailableTimeResponseDto> actual = RestAssuredMockMvc.given()
                    .queryParam("localDate", "2026-05-10")
                    .when().get("/themes/" + theme.getId() + "/times")
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(new TypeRef<>() {});

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("인기 테마 목록을 조회하면 200을 반환한다")
        void returnsPopularThemes() {
            given(themeService.findPopulars(any())).willReturn(List.of(theme));
            List<ThemeResponseDto> expected = List.of(ThemeResponseDto.from(theme));

            List<ThemeResponseDto> actual = RestAssuredMockMvc.given()
                    .queryParam("limit", "10")
                    .queryParam("days", "7")
                    .when().get("/themes/populars")
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(new TypeRef<>() {});

            assertThat(actual).isEqualTo(expected);
        }
    }
}
