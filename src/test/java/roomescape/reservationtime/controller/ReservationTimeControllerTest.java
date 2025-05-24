package roomescape.reservationtime.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.service.ReservationTimeService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationTimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationTimeService reservationTimeService;

    private final LocalTime testTime = LocalTime.of(13, 0);

    @BeforeEach
    void setUp() {
        reservationTimeRepository.deleteAll();
    }

    @Test
    void 예약_시간_생성_성공() throws Exception {
        // given
        ReservationTimeRequest request = new ReservationTimeRequest(testTime);

        // when & then
        mockMvc.perform(post("/times")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"startAt\": \"13:00\"}")
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("id").exists())
        .andExpect(jsonPath("startAt").value("13:00"));
    }

    @Test
    void 모든_예약_시간_조회_성공() throws Exception {
        // given
        reservationTimeService.saveTime(new ReservationTimeRequest(LocalTime.of(13, 0)));
        reservationTimeService.saveTime(new ReservationTimeRequest(LocalTime.of(14, 0)));

        // when & then
        mockMvc.perform(get("/times")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath("[0].startAt", is("13:00")))
        .andExpect(jsonPath("[1].startAt", is("14:00")));
    }

    @Test
    void 예약_시간_삭제_성공() throws Exception {
        // given
        ReservationTimeResponse response = reservationTimeService.saveTime(new ReservationTimeRequest(testTime));
        Long id = response.id();

        // when & then
        mockMvc.perform(delete("/times/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
    }

    @Test
    void 존재하지_않는_예약_시간_삭제_실패() throws Exception {
        // given
        // when & then
        mockMvc.perform(delete("/times/999")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
    }
}
