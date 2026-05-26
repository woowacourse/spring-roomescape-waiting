package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.domain.ReservationTime;

class ReservationTimeDaoTest {

    private EmbeddedDatabase dataSource;
    private ReservationTimeDao reservationTimeDao;

    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
        reservationTimeDao = new ReservationTimeDao(new JdbcTemplate(dataSource));
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    @Test
    void findAll_전체_시간_조회() {
        assertThat(reservationTimeDao.findAll()).isNotEmpty();
    }

    @Test
    void findById_존재하는_id이면_반환() {
        Optional<ReservationTime> result = reservationTimeDao.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void findById_존재하지_않는_id이면_empty() {
        Optional<ReservationTime> result = reservationTimeDao.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void existsByStartAt_존재하면_true() {
        assertThat(reservationTimeDao.existsByStartAt(LocalTime.of(10, 0))).isTrue();
    }

    @Test
    void existsByStartAt_없으면_false() {
        assertThat(reservationTimeDao.existsByStartAt(LocalTime.of(23, 0))).isFalse();
    }

    @Test
    void save_시간_저장() {
        ReservationTime saved = reservationTimeDao.save(LocalTime.of(23, 0));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(23, 0));
    }

    @Test
    void delete_시간_삭제() {
        reservationTimeDao.delete(12L);

        assertThat(reservationTimeDao.findById(12L)).isEmpty();
    }
}
