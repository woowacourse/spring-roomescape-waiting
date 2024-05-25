package roomescape.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.CreateTimeResponse;
import roomescape.global.exception.RoomescapeException;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationTimeService reservationTimeService;

    private final String rawTime = "19:00";

    @DisplayName("성공: 예약 시간을 저장하고, id 값과 함께 반환한다.")
    @Test
    void save() {
        CreateTimeResponse saved = reservationTimeService.save(rawTime);
        assertThat(saved.id()).isEqualTo(1L);
    }

    @DisplayName("실패: 이미 존재하는 시간을 추가할 수 없다.")
    @Test
    void save_TimeAlreadyExists() {
        reservationTimeService.save(rawTime);

        assertThatThrownBy(() -> reservationTimeService.save(rawTime))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("이미 존재하는 시간은 추가할 수 없습니다.");
    }

    @DisplayName("실패: 시간을 사용하는 예약이 존재하는 경우 시간을 삭제할 수 없다.")
    @Test
    void delete_ReservationExists() {
        jdbcTemplate.update("""
            INSERT INTO member(name, email, password, role)
            VALUES ('러너덕', 'user@a.com', '123a!', 'USER');
                        
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg');
                        
            INSERT INTO reservation_time(start_at)
            VALUES ('08:00');
                        
            INSERT INTO reservation(member_id, reserved_date, created_at, time_id, theme_id, status)
            VALUES (1, '2060-01-01', '2024-01-01', 1, 1, 'RESERVED');
            """);

        assertThatThrownBy(() -> reservationTimeService.delete(1L))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간을 사용하는 예약이 존재하여 삭제할 수 없습니다.");
    }
}
