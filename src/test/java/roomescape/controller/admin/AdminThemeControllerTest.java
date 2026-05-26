package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.Optional;
import roomescape.dao.MemberDao;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Theme;
import roomescape.domain.vo.Name;
import roomescape.dto.request.ThemeRequestDto;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.service.ThemeService;

import static org.mockito.BDDMockito.given;

@WebMvcTest(AdminThemeController.class)
class AdminThemeControllerTest {

    private final Member admin = new Member(1L, "어드민", "admin@test.com", "password", MemberRole.ADMIN);
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
        given(memberDao.findById(admin.getId())).willReturn(Optional.of(admin));
    }

    @Nested
    class Get {

        @Test
        @DisplayName("전체 테마 목록을 조회하면 200을 반환한다")
        void returnsAllThemes() {
            given(themeService.findAll()).willReturn(List.of(theme));
            List<ThemeResponseDto> expected = List.of(ThemeResponseDto.from(theme));

            List<ThemeResponseDto> actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().get("/admin/themes")
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
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().get("/admin/themes/" + theme.getId())
                    .then()
                    .status(HttpStatus.OK)
                    .extract().as(ThemeResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Post {

        @Test
        @DisplayName("유효한 요청으로 테마를 생성하면 201을 반환한다")
        void createsTheme() {
            ThemeRequestDto requestDto = new ThemeRequestDto(
                    theme.getName().getValue(), theme.getThumbnailUrl(), theme.getDescription());
            given(themeService.create(any())).willReturn(theme);
            ThemeResponseDto expected = ThemeResponseDto.from(theme);

            ThemeResponseDto actual = RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(requestDto)
                    .when().post("/admin/themes")
                    .then()
                    .status(HttpStatus.CREATED)
                    .header("Location", "http://localhost/admin/themes/" + theme.getId())
                    .extract().as(ThemeResponseDto.class);

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class Delete {

        @Test
        @DisplayName("테마를 삭제하면 204를 반환한다")
        void deletesTheme() {
            willDoNothing().given(themeService).delete(theme.getId());

            RestAssuredMockMvc.given()
                    .sessionAttr("memberId", String.valueOf(admin.getId()))
                    .when().delete("/admin/themes/" + theme.getId())
                    .then()
                    .status(HttpStatus.NO_CONTENT);

            then(themeService).should().delete(theme.getId());
        }
    }
}
