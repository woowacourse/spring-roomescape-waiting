package roomescape.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.application.ReservationTimeService;
import roomescape.application.ThemeService;

/**
 * UserThemeController 슬라이스 테스트 (@WebMvcTest).
 *
 * <p>고유 책임: 예약 가능 시간 조회의 date 쿼리 파라미터 처리.
 * date 누락 → MissingServletRequestParameterException → 400,
 * date 타입 오류(abc) → MethodArgumentTypeMismatchException → 400.
 * 이 흐름은 HTTP 파라미터 바인딩 단계라 서비스 직접 호출로는 검증되지 않는, 이 슬라이스의 고유 가치다.
 *
 * <p>이 컨트롤러는 ThemeService와 ReservationTimeService 둘 다 의존하므로 @MockBean 2개가 필요하다.
 */
@WebMvcTest(UserThemeController.class)
class UserThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThemeService themeService;

    @MockBean
    private ReservationTimeService reservationTimeService;

    @Nested
    @DisplayName("테마 목록 GET /user/themes")
    class ThemeList {

        @Test
        @DisplayName("200과 목록을 반환한다")
        void 목록_조회() throws Exception {
            given(themeService.findAll()).willReturn(List.of());

            mockMvc.perform(get("/user/themes"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("예약 가능 시간 GET /user/themes/{themeId}/available-times")
    class AvailableTimes {

        @Test
        @DisplayName("정상 요청이면 200")
        void 정상_조회() throws Exception {
            given(reservationTimeService.findAvailable(any(), anyLong())).willReturn(List.of());

            mockMvc.perform(get("/user/themes/1/available-times").param("date", "2050-12-31"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[입력 게이트] date 파라미터가 누락되면 400")
        void date_누락_400() throws Exception {
            mockMvc.perform(get("/user/themes/1/available-times"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 누락되었습니다."));
        }

        @Test
        @DisplayName("[입력 게이트] themeId 경로변수 타입 오류면 400")
        void themeId_타입오류_400() throws Exception {
            mockMvc.perform(get("/user/themes/abc/available-times").param("date", "2050-12-31"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("요청 파라미터 형식이 올바르지 않습니다."));
        }

        @Test
        @DisplayName("[입력 게이트] date 형식 오류면 400")
        void date_형식오류_400() throws Exception {
            mockMvc.perform(get("/user/themes/1/available-times").param("date", "not-a-date"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("요청 파라미터 형식이 올바르지 않습니다."));
        }
    }

    @Nested
    @DisplayName("인기 테마 GET /user/themes/popular")
    class Popular {

        @Test
        @DisplayName("200과 인기 목록을 반환한다")
        void 인기_조회() throws Exception {
            given(themeService.findPopular()).willReturn(List.of());

            mockMvc.perform(get("/user/themes/popular"))
                    .andExpect(status().isOk());
        }
    }
}
