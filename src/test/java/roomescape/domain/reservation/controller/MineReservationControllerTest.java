package roomescape.domain.reservation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.ReservationEditableStatus;
import roomescape.domain.reservation.service.ReservationService;
import roomescape.domain.theme.dto.response.ReservationThemeResponseDto;
import roomescape.domain.time.dto.response.ReservationTimeResponseDto;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.TimeFixture;

@WebMvcTest(MineReservationController.class)
class MineReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    private ReservationResponseDto sampleResponse() {
        return new ReservationResponseDto(
            1L,
            ReservationFixture.FUTURE.getName(),
            ReservationFixture.FUTURE.getDate(),
            new ReservationTimeResponseDto(1L, TimeFixture.VALID_10_00.getStartAt(), false),
            new ReservationThemeResponseDto(
                1L,
                ThemeFixture.VALID.getName(),
                ThemeFixture.VALID.getDescription(),
                ThemeFixture.VALID.getImageUrl(),
                false
            ),
            ReservationEditableStatus.EDITABLE,
            "",
            null,
            0L
        );
    }

    @Nested
    class 내_예약_조회 {

        @Test
        void 예약자_이름으로_본인의_예약_목록을_조회한다() throws Exception {
            when(reservationService.getMineReservations(ReservationFixture.FUTURE.getName()))
                .thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/reservations-mine")
                    .queryParam("name", ReservationFixture.FUTURE.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", equalTo(ReservationFixture.FUTURE.getName())));
        }

        @Test
        void name_파라미터가_없으면_4xx를_반환한다() throws Exception {
            mockMvc.perform(get("/reservations-mine"))
                .andExpect(status().is4xxClientError());
        }

        @Test
        void 빈_name으로_조회하면_4xx를_반환한다() throws Exception {
            mockMvc.perform(get("/reservations-mine")
                    .queryParam("name", ""))
                .andExpect(status().is4xxClientError());
        }
    }
}
