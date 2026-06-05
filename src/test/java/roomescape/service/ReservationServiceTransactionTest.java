package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Sql({"/schema.sql", "/test-data.sql"})
class ReservationServiceTransactionTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 저장 중 예외가 발생하면 슬롯 예약 상태 변경을 롤백한다.")
    void rollbackThemeSlotReservedWhenSaveReservationFails() {
        long themeSlotId = insertThemeSlot(LocalDate.now().plusDays(30));
        doThrow(new RuntimeException("예약 저장 실패"))
                .when(reservationRepository)
                .save(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.saveReservation("브라운", themeSlotId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("예약 저장 실패");

        assertThat(findThemeSlotReserved(themeSlotId)).isFalse();
    }

    private long insertThemeSlot(LocalDate date) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO theme_slot (theme_id, date, time_id, is_reserved)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, 1L);
            ps.setObject(2, date);
            ps.setLong(3, 1L);
            ps.setBoolean(4, false);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private boolean findThemeSlotReserved(long themeSlotId) {
        return jdbcTemplate.queryForObject("""
                        SELECT is_reserved
                        FROM theme_slot
                        WHERE id = ?
                        """,
                Boolean.class,
                themeSlotId
        );
    }
}
