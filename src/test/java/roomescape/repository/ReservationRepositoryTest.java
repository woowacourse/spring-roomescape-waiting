package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.repository.dto.ReservationWithRank;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("성공: 예약 대기 정보 + 순번 조회")
    @Test
    void findReservationsWithRankByMemberId() {
        jdbcTemplate.update("""
            INSERT INTO member(name, email, password, role)
            VALUES ('러너덕', 'deock@a.com', '123a!', 'USER'),
                   ('트레', 'tre@a.com', '123a!', 'USER'),
                   ('안돌', 'andol@a.com', '123a!', 'USER'),
                   ('제이', 'jay@a.com', '123a!', 'USER'),
                   ('포비', 'poby@a.com', '123a!', 'USER');
                   
            INSERT INTO theme(name, description, thumbnail)
            VALUES ('테마1', 'd1', 'https://test.com/test1.jpg');
                   
            INSERT INTO reservation_time(start_at)
            VALUES ('08:00');
                   
            INSERT INTO reservation(member_id, reserved_date, created_at, time_id, theme_id, status)
            VALUES (1, TIMESTAMPADD(DAY, 2, CURRENT_DATE), TIMESTAMPADD(DAY, -5, CURRENT_DATE), 1, 1, 'RESERVED'),
                   (2, TIMESTAMPADD(DAY, 2, CURRENT_DATE), TIMESTAMPADD(DAY, -4, CURRENT_DATE), 1, 1, 'STANDBY'),
                   (3, TIMESTAMPADD(DAY, 2, CURRENT_DATE), TIMESTAMPADD(DAY, -1, CURRENT_DATE), 1, 1, 'STANDBY'),
                   (4, TIMESTAMPADD(DAY, 2, CURRENT_DATE), TIMESTAMPADD(DAY, -2, CURRENT_DATE), 1, 1, 'STANDBY'),
                   (5, TIMESTAMPADD(DAY, 2, CURRENT_DATE), TIMESTAMPADD(DAY, -3, CURRENT_DATE), 1, 1, 'STANDBY');
            """);

        assertThat(reservationRepository.findReservationsWithRankByMemberId(5L))
            .extracting(ReservationWithRank::rank)
            .containsExactly(2L);
    }
}
