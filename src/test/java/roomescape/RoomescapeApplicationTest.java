package roomescape;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoomescapeApplicationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        private final static LocalDate tomorrow = LocalDate.now().plusDays(1);

        @BeforeEach
        void setUp() {
                jdbcTemplate.update("delete from waiting");
                jdbcTemplate.update("delete from reservation");
                jdbcTemplate.update("delete from reservation_time");
                jdbcTemplate.update("delete from theme");

                jdbcTemplate.update("alter table waiting alter column id restart with 1");
                jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
                jdbcTemplate.update("alter table theme alter column id restart with 1");

                jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
                jdbcTemplate.update("insert into theme (name, description, url) values ('테스트', '설명', 'url')");
                jdbcTemplate.update("insert into reservation (name, date, time_id, theme_id, created_at) values ('다른사람', ?, 1, 1, '2026-05-15 10:30:00')", tomorrow);
                jdbcTemplate.update("insert into waiting (name, date, time_id, theme_id, created_at) values ('테스트', ?, 1, 1, '2026-05-15 10:30:00')", tomorrow);
        }

        @Test
        void 예약_대기열이_정상_생성된다() throws Exception {
                ReservationWaitingRequest reservationWaitingRequest = new ReservationWaitingRequest("테스트2", tomorrow, 1L, 1L);

                mockMvc.perform(post("/reservations/waitings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reservationWaitingRequest)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("테스트2"))
                        .andExpect(jsonPath("$.date").value(tomorrow.toString()))
                        .andExpect(jsonPath("$.time.id").value(1L))
                        .andExpect(jsonPath("$.time.startAt").value("10:00"))
                        .andExpect(jsonPath("$.theme.id").value(1L))
                        .andExpect(jsonPath("$.theme.name").value("테스트"))
                        .andExpect(jsonPath("$.sequence").exists());
        }

        @Test
        void 예약_대기열_정상_삭제된다() throws Exception {
                mockMvc.perform(delete("/reservations/waitings/1"))
                        .andExpect(status().isNoContent());
        }

        @Test
        void 전체_예약_대기열이_정상적으로_조회된다() throws Exception {
                mockMvc.perform(get("/reservations/waitings"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").value(1L))
                        .andExpect(jsonPath("$[0].name").value("테스트"))
                        .andExpect(jsonPath("$[0].date").value(tomorrow.toString()))
                        .andExpect(jsonPath("$[0].time.id").value(1L))
                        .andExpect(jsonPath("$[0].time.startAt").value("10:00"))
                        .andExpect(jsonPath("$[0].theme.id").value(1L))
                        .andExpect(jsonPath("$[0].theme.name").value("테스트"))
                        .andExpect(jsonPath("$[0].sequence").value(1L));
        }

        @Test
        void 이름으로_예약_대기열_조회가_정상적으로_반환된다() throws Exception {
                mockMvc.perform(get("/reservations/waitings/mine")
                                .param("name", "테스트"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").value(1L))
                        .andExpect(jsonPath("$[0].name").value("테스트"))
                        .andExpect(jsonPath("$[0].date").value(tomorrow.toString()))
                        .andExpect(jsonPath("$[0].time.id").value(1L))
                        .andExpect(jsonPath("$[0].time.startAt").value("10:00"))
                        .andExpect(jsonPath("$[0].theme.id").value(1L))
                        .andExpect(jsonPath("$[0].theme.name").value("테스트"))
                        .andExpect(jsonPath("$[0].sequence").value(1L));
        }
}