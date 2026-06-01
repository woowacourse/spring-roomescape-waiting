package roomescape.date.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.common.TestAuthRequestPostProcessors.member;

import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.annotation.ControllerSliceTest;
import roomescape.common.auth.jwt.JwtExtractor;
import roomescape.common.auth.jwt.JwtValidator;
import roomescape.date.domain.ReservationDate;
import roomescape.date.service.ReservationDateService;

@ControllerSliceTest(controllers = ReservationDateController.class)
class ReservationDateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationDateService reservationDateService;

    @MockitoBean
    private JwtExtractor jwtExtractor;

    @MockitoBean
    private JwtValidator jwtValidator;

    @Nested
    @DisplayName("getReservationDates 메서드는")
    class GetReservationDates {

        @Test
        @DisplayName("예약 날짜 목록을 조회한다")
        void getReservationDates() throws Exception {
            // given
            LocalDate date = LocalDate.parse("2026-05-20");
            ReservationDate reservationDate = ReservationDate.load(1L, date, true);

            // when
            when(reservationDateService.readDatesAfterToday())
                .thenReturn(Collections.singletonList(reservationDate));

            // then
            mockMvc.perform(get("/member/dates").with(member()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservationDate.getId()))
                .andExpect(jsonPath("$[0].date").value(date.toString()))
                .andExpect(jsonPath("$[0].isActive").value(reservationDate.isActive()));
        }
    }
}