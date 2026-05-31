package roomescape.holiday.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.holiday.domain.Holiday;
import roomescape.holiday.service.HolidayService;
import roomescape.holiday.service.dto.HolidaySaveServiceDto;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminHolidayController.class)
class AdminHolidayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HolidayService holidayService;

    @DisplayName("전체 휴일 목록을 조회한다.")
    @Test
    void 휴일_목록_조회() throws Exception {
        Holiday holiday1 = new Holiday(1L, LocalDate.of(2026,5,6));
        Holiday holiday2 = new Holiday(2L, LocalDate.of(2026,6,6));
        Holiday holiday3 = new Holiday(3L, LocalDate.of(2026,7,6));
        List<Holiday> holidayList = List.of(holiday1,holiday2,holiday3);

        Mockito.when(holidayService.getAll()).thenReturn(holidayList);

        mockMvc.perform(get("/admin/holidays")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("날짜로 휴일을 생성한다.")
    @Test
    void 휴일_생성() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 8);
        Holiday savedHoliday = new Holiday(1L, date);

        Mockito.when(holidayService.create(Mockito.any(HolidaySaveServiceDto.class)))
                .thenReturn(savedHoliday);

        String requestBody = String.format("{\"id\":%d,\"date\":\"%s\"}", 1L, date);

        mockMvc.perform(post("/admin/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @DisplayName("휴일 ID로 휴일을 삭제한다.")
    @Test
    void 휴일_삭제() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/admin/holidays/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(holidayService).delete(id);
    }

    @DisplayName("날짜 없이 휴일 생성 요청인 경우, 400을 반환한다.")
    @Test
    void 날짜_누락_휴일_생성_400() throws Exception {
        String requestBody = """
                {
                    "id": 1
                }
                """;

        mockMvc.perform(post("/admin/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("잘못된 날짜 형식으로 휴일 생성 요청인 경우, 400을 반환한다.")
    @Test
    void 잘못된_날짜_형식_휴일_생성_400() throws Exception {
        String requestBody = """
                {
                    "id": 1,
                    "date": "2026/08/08"
                }
                """;

        mockMvc.perform(post("/admin/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
