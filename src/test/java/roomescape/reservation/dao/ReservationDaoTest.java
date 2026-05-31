package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;

@JdbcTest
class ReservationDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationDao reservationDao;
    private ReservationTimeDao reservationTimeDao;
    private ThemeDao themeDao;

    @BeforeEach
    void setUp() {
        reservationDao = new ReservationDao(jdbcTemplate);
        reservationTimeDao = new ReservationTimeDao(jdbcTemplate);
        themeDao = new ThemeDao(jdbcTemplate);
    }

    private ReservationTime createTime() {
        return reservationTimeDao.insert(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));
    }

    private Theme createTheme() {
        return themeDao.insert(new ThemeCreateRequest("테마이름", "테마설명", "https://image.url"));
    }

    @Nested
    class 예약을_저장한다 {

        @Test
        void 새로운_예약을_저장한다() {
            // given
            ReservationTime time = createTime();
            Theme theme = createTheme();

            // when
            Reservation saved = reservationDao.insert("브라운", LocalDate.of(2023, 8, 5), time, theme, ReservationStatus.RESERVED);

            // then
            assertAll(
                    () -> assertThat(saved.getId()).isNotNull(),
                    () -> assertThat(saved.getName()).isEqualTo("브라운"),
                    () -> assertThat(saved.getTime().getId()).isEqualTo(time.getId()),
                    () -> assertThat(saved.getTheme().getId()).isEqualTo(theme.getId())
            );
        }

        @Test
        void 저장_후_전체_조회에_포함된다() {
            // given
            ReservationTime time = createTime();
            Theme theme = createTheme();

            // when
            reservationDao.insert("브라운", LocalDate.of(2023, 8, 5), time, theme, ReservationStatus.RESERVED);

            // then
            List<Reservation> all = reservationDao.findAll();
            assertThat(all).hasSize(1);
        }
    }

    @Nested
    class 예약을_조회한다 {

        @Test
        @Sql(statements = {
                "INSERT INTO theme (id, name, description, image_url) VALUES (1, 't1', '설명', 'url')",
                "INSERT INTO theme (id, name, description, image_url) VALUES (2, 't2', '설명', 'url')",
                "INSERT INTO reservation_time (id, start_at) VALUES (1, '10:10')",
                "INSERT INTO reservation_time (id, start_at) VALUES (2, '10:20')",
                "INSERT INTO reservation (id, name, date, time_id, theme_id, status) VALUES (1, '브라운', '2023-08-05', 1, 1, 'RESERVED')",
                "INSERT INTO reservation (id, name, date, time_id, theme_id, status) VALUES (2, '조다현', '2023-08-05', 2, 2, 'RESERVED')"
        })
        void 저장된_모든_예약을_조회한다() {
            // given
            // when
            List<Reservation> all = reservationDao.findAll();

            // then
            assertAll(
                    () -> assertThat(all).hasSize(2),
                    () -> assertThat(all.getFirst().getId()).isEqualTo(1),
                    () -> assertThat(all.getFirst().getName()).isEqualTo("브라운"),
                    () -> assertThat(all.getFirst().getDate()).isEqualTo(LocalDate.of(2023, 8, 5)),
                    () -> assertThat(all.getFirst().getTime().getId()).isEqualTo(1),
                    () -> assertThat(all.getFirst().getTheme().getId()).isEqualTo(1),
                    () -> assertThat(all.get(1).getId()).isEqualTo(2),
                    () -> assertThat(all.get(1).getName()).isEqualTo("조다현"),
                    () -> assertThat(all.get(1).getDate()).isEqualTo(LocalDate.of(2023, 8, 5)),
                    () -> assertThat(all.get(1).getTime().getId()).isEqualTo(2),
                    () -> assertThat(all.get(1).getTheme().getId()).isEqualTo(2)
            );
        }

        @Test
        @Sql(statements = {
                "INSERT INTO theme (id, name, description, image_url) VALUES (1, 't1', '설명', 'url')",
                "INSERT INTO reservation_time (id, start_at) VALUES (1, '10:10')",
                "INSERT INTO reservation (id, name, date, time_id, theme_id, status) VALUES (1, '브라운', '2023-08-05', 1, 1, 'RESERVED')"
        })
        void ID로_예약을_조회한다() {
            // given
            // when
            Reservation reservation = reservationDao.findById(1L);

            // then
            assertAll(
                    () -> assertThat(reservation.getName()).isEqualTo("브라운"),
                    () -> assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2023, 8, 5)),
                    () -> assertThat(reservation.getTime().getId()).isEqualTo(1),
                    () -> assertThat(reservation.getTheme().getId()).isEqualTo(1)
            );
        }
    }

    @Test
    void ID로_예약을_삭제한다() {
        // given
        ReservationTime time = createTime();
        Theme theme = createTheme();
        Reservation saved = reservationDao.insert("브라운", LocalDate.of(2023, 8, 5), time, theme, ReservationStatus.RESERVED);

        // when
        reservationDao.delete(saved.getId());

        // then
        List<Reservation> all = reservationDao.findAll();
        assertThat(all).isEmpty();
    }

    @Nested
    class 시간_ID로_예약_존재_여부를_확인한다 {

        @Test
        void 해당_시간의_예약이_존재하면_true를_반환한다() {
            // given
            ReservationTime time = createTime();
            Theme theme = createTheme();
            reservationDao.insert("브라운", LocalDate.of(2023, 8, 5), time, theme, ReservationStatus.RESERVED);

            // when
            boolean exists = reservationDao.existsByTimeId(time.getId());

            // then
            assertThat(exists).isTrue();
        }

        @Test
        void 해당_시간의_예약이_없으면_false를_반환한다() {
            // when
            boolean exists = reservationDao.existsByTimeId(999L);

            // then
            assertThat(exists).isFalse();
        }
    }
}
