package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
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
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ThemeSimpleResponse;
import roomescape.reservation.dto.response.TimeResponse;
import roomescape.theme.dao.ThemeDAO;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;

@JdbcTest
class ReservationDAOTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationDAO reservationDAO;
    private ReservationTimeDAO reservationTimeDAO;
    private ThemeDAO themeDAO;

    @BeforeEach
    void setUp() {
        reservationDAO = new ReservationDAO(jdbcTemplate);
        reservationTimeDAO = new ReservationTimeDAO(jdbcTemplate);
        themeDAO = new ThemeDAO(jdbcTemplate);
    }

    private ReservationTimeCreateResponse createTime() {
        ReservationTime reservationTime = reservationTimeDAO.insert(new ReservationTimeCreateRequest(LocalTime.of(10, 0)));

        return ReservationTimeCreateResponse.of(reservationTime.getId(), LocalTime.of(10, 10));
    }

    private Theme createTheme() {
        return themeDAO.insert(new ThemeCreateRequest("테마이름", "테마설명", "https://image.url"));
    }

    @Nested
    class 예약을_저장한다 {

        @Test
        void 새로운_예약을_저장한다() {
            // given
            ReservationTimeCreateResponse time = createTime();
            Theme theme = createTheme();

            // when
            Reservation saved = reservationDAO.insert("브라운", LocalDate.of(2023, 8, 5), time.id(), theme.getId(), ReservationStatus.RESERVED);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("브라운");
            assertThat(saved.getTime().getId()).isEqualTo(time.id());
            assertThat(saved.getTheme().getId()).isEqualTo(theme.getId());
        }

        @Test
        void 저장_후_전체_조회에_포함된다() {
            // given
            ReservationTimeCreateResponse time = createTime();
            Theme theme = createTheme();

            // when
            reservationDAO.insert("브라운", LocalDate.of(2023, 8, 5), time.id(), theme.getId(), ReservationStatus.RESERVED);

            // then
            List<ReservationResponse> all = reservationDAO.findAll().stream()
                    .map(reservation -> ReservationResponse.of(
                            reservation.getId(),
                            reservation.getName(),
                            reservation.getDate(),
                            TimeResponse.from(reservation.getTime()),
                            ThemeSimpleResponse.from(reservation.getTheme())
                    )).toList();
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
            List<ReservationResponse> all = reservationDAO.findAll().stream()
                    .map(reservation -> ReservationResponse.of(
                            reservation.getId(),
                            reservation.getName(),
                            reservation.getDate(),
                            TimeResponse.from(reservation.getTime()),
                            ThemeSimpleResponse.from(reservation.getTheme())
                    )).toList();

            // then
            assertThat(all).hasSize(2);
            assertThat(all.getFirst().id()).isEqualTo(1);
            assertThat(all.getFirst().name()).isEqualTo("브라운");
            assertThat(all.getFirst().date()).isEqualTo(LocalDate.of(2023, 8, 5));
            assertThat(all.getFirst().time().id()).isEqualTo(1);
            assertThat(all.getFirst().theme().id()).isEqualTo(1);
            assertThat(all.get(1).id()).isEqualTo(2);
            assertThat(all.get(1).name()).isEqualTo("조다현");
            assertThat(all.get(1).date()).isEqualTo(LocalDate.of(2023, 8, 5));
            assertThat(all.get(1).time().id()).isEqualTo(2);
            assertThat(all.get(1).theme().id()).isEqualTo(2);
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
            Reservation reservation = reservationDAO.findById(1L);

            // then
            Assertions.assertThat(reservation.getName()).isEqualTo("브라운");
            Assertions.assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2023, 8, 5));
            Assertions.assertThat(reservation.getTime().getId()).isEqualTo(1);
            Assertions.assertThat(reservation.getTheme().getId()).isEqualTo(1);
        }
    }

    @Test
    void ID로_예약을_삭제한다() {
        // given
        ReservationTimeCreateResponse time = createTime();
        Theme theme = createTheme();
        Reservation saved = reservationDAO.insert("브라운", LocalDate.of(2023, 8, 5), time.id(), theme.getId(), ReservationStatus.RESERVED);

        // when
        reservationDAO.delete(saved.getId());

        // then
        List<ReservationResponse> all = reservationDAO.findAll().stream()
                .map(reservation -> ReservationResponse.of(
                        reservation.getId(),
                        reservation.getName(),
                        reservation.getDate(),
                        TimeResponse.from(reservation.getTime()),
                        ThemeSimpleResponse.from(reservation.getTheme())
                )).toList();
        assertThat(all).isEmpty();
    }

    @Nested
    class 시간_ID로_예약_존재_여부를_확인한다 {

        @Test
        void 해당_시간의_예약이_존재하면_true를_반환한다() {
            // given
            ReservationTimeCreateResponse time = createTime();
            Theme theme = createTheme();
            reservationDAO.insert("브라운", LocalDate.of(2023, 8, 5), time.id(), theme.getId(), ReservationStatus.RESERVED);

            // when
            boolean exists = reservationDAO.existsByTimeId(time.id());

            // then
            assertThat(exists).isTrue();
        }

        @Test
        void 해당_시간의_예약이_없으면_false를_반환한다() {
            // when
            boolean exists = reservationDAO.existsByTimeId(999L);

            // then
            assertThat(exists).isFalse();
        }
    }
}
