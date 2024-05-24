package roomescape.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.reservation.controller.dto.ThemeResponse;
import roomescape.util.ControllerTest;

@DisplayName("테마 API 통합 테스트")
class ThemeControllerTest extends ControllerTest {

    @DisplayName("테마 생성 시, 201을 반환한다.")
    @Test
    void create() throws Exception {
        //given
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");

        Map<String, String> params = new HashMap<>();
        params.put("name", "siso");
        params.put("description", "Hi, I am Siso");
        params.put("thumbnail", "thumbnail");

        //when
        doReturn(themeResponse)
                .when(themeService)
                .create(any());

        //then
        mockMvc.perform(
                post("/themes")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isCreated());
    }

    @DisplayName("테마 조회 시, 200을 반환한다.")
    @Test
    void findAll() throws Exception {
        //given
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");

        //when
        doReturn(List.of(themeResponse))
                .when(themeService)
                .findAllThemes();

        //then
        mockMvc.perform(
                get("/themes")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("테마 삭제 시, 204를 반환한다.")
    @Test
    void deleteTest() throws Exception {
        //given
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");

        //when
        doNothing()
                .when(themeService)
                .delete(isA(Long.class));

        //then
        mockMvc.perform(
                delete("/themes/" + themeResponse.id())
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());
    }

    @DisplayName("예약이 있는 시간 삭제 시, 400을 반환한다.")
    @Test
    void delete_WithReservationTime() throws Exception {
        //given
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");

        //when
        doThrow(new BadRequestException(ErrorType.RESERVATION_NOT_DELETED))
                .when(themeService)
                .delete(isA(Long.class));

        //then
        mockMvc.perform(
                delete("/themes/" + themeResponse.id())
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("인기 테마 조회 시, 200을 반환한다.")
    @Test
    void getAvailable() throws Exception {
        //given
        ThemeResponse themeResponse = new ThemeResponse(3L, "이름", "설명", "썸네일");

        //when
        doReturn(List.of(themeResponse))
                .when(themeService)
                .findPopularThemes(isA(LocalDate.class), isA(LocalDate.class), isA(Integer.class));

        //then
        mockMvc.perform(
                get("/themes/popular")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @DisplayName("테마 생성 시, 잘못된 테마 이름 형식에 대해 400을 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "      "})
    void create_Invalid_Name(String name) throws Exception {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", "Hi, I am Siso");
        params.put("thumbnail", "thumbnail");

        //when
        doThrow(new BadRequestException(ErrorType.NAME_FORMAT_ERROR))
                .when(themeService)
                .create(any());

        //then
        mockMvc.perform(
                post("/themes")
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params))
        ).andExpect(status().isBadRequest());
    }

    @DisplayName("적절하지 않은 limit에 대해 400을 반환한다.")
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 100})
    void invalidLimit(int limit) throws Exception {
        //given
        String startDate = "2024-05-03";
        String endDate = "2024-05-06";

        //when
        doThrow(new BadRequestException(ErrorType.NAME_FORMAT_ERROR))
                .when(themeService)
                .create(any());

        //then
        mockMvc.perform(
                get(String.format("/themes/popular?startDate=%s&endDate=%s&limit=%s", startDate, endDate, limit))
                        .cookie(new Cookie("token", adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }
}
