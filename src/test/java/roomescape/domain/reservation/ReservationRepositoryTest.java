package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@JdbcTest
@Import(ReservationRepository.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationTime time;
    private ReservationTime anotherTime;
    private Theme theme;
    private Theme anotherTheme;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00:00', '11:00:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('12:00:00', '13:00:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES ('테마1', '설명1', 'https://example.com/1.jpg')"
        );
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES ('테마2', '설명2', 'https://example.com/2.jpg')"
        );

        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = '10:00:00'",
                Long.class);
        Long anotherTimeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = '12:00:00'",
                Long.class);
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마1'", Long.class);
        Long anotherThemeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마2'", Long.class);

        time = ReservationTime.of(timeId, LocalTime.of(10, 0), LocalTime.of(11, 0));
        anotherTime = ReservationTime.of(anotherTimeId, LocalTime.of(12, 0), LocalTime.of(13, 0));
        theme = Theme.of(themeId, "테마1", "설명1", "https://example.com/1.jpg");
        anotherTheme = Theme.of(anotherThemeId, "테마2", "설명2", "https://example.com/2.jpg");
    }

    @Nested
    @DisplayName("예약 저장")
    class Save {

        @Test
        void 저장하면_id가_부여된다() {
            Reservation reservation = Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme);

            Reservation saved = reservationRepository.save(reservation);

            assertAll(
                    () -> assertThat(saved.getId()).isNotNull(),
                    () -> assertThat(saved.getName()).isEqualTo("유저1"),
                    () -> assertThat(saved.getDate()).isEqualTo(LocalDate.of(2099, 12, 31)),
                    () -> assertThat(saved.getTime().getId()).isEqualTo(time.getId()),
                    () -> assertThat(saved.getTheme().getId()).isEqualTo(theme.getId())
            );
        }
    }

    @Nested
    @DisplayName("날짜/시간/테마로 존재 여부 조회")
    class ExistsByDateAndTimeIdAndThemeId {

        @Test
        void 예약이_존재하면_true를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            boolean result = reservationRepository.existsByDateAndTimeIdAndThemeId(
                    LocalDate.of(2099, 12, 31), time.getId(), theme.getId()
            );

            assertThat(result).isTrue();
        }

        @Test
        void 날짜가_다르면_false를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            boolean result = reservationRepository.existsByDateAndTimeIdAndThemeId(
                    LocalDate.of(2099, 12, 30), time.getId(), theme.getId()
            );

            assertThat(result).isFalse();
        }

        @Test
        void 시간이_다르면_false를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            boolean result = reservationRepository.existsByDateAndTimeIdAndThemeId(
                    LocalDate.of(2099, 12, 31), anotherTime.getId(), theme.getId()
            );

            assertThat(result).isFalse();
        }

        @Test
        void 테마가_다르면_false를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            boolean result = reservationRepository.existsByDateAndTimeIdAndThemeId(
                    LocalDate.of(2099, 12, 31), time.getId(), anotherTheme.getId()
            );

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("예약된 시간 조회")
    class FindTimeByDateAndThemeId {

        @Test
        void 날짜와_테마에_해당하는_예약_시간_id를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));
            reservationRepository.save(Reservation.of("유저2", LocalDate.of(2099, 12, 31), anotherTime, theme));
            reservationRepository.save(Reservation.of("유저3", LocalDate.of(2099, 12, 30), time, theme));
            reservationRepository.save(Reservation.of("유저4", LocalDate.of(2099, 12, 31), time, anotherTheme));

            List<Long> result = reservationRepository.findTimeByDateAndThemeId(
                    LocalDate.of(2099, 12, 31), theme.getId()
            );

            assertThat(result).containsExactly(time.getId(), anotherTime.getId());
        }
    }

    @Nested
    @DisplayName("id로 예약 삭제")
    class DeleteById {

        @Test
        void 삭제하면_해당_예약이_조회되지_않는다() {
            Reservation saved = reservationRepository.save(
                    Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme)
            );

            reservationRepository.deleteById(saved.getId());

            assertThat(reservationRepository.existsById(saved.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("테마 id로 예약 존재 여부 조회")
    class ExistsByThemeId {

        @Test
        void 해당_테마의_예약이_존재하면_true를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            boolean result = reservationRepository.existsByThemeId(theme.getId());

            assertThat(result).isTrue();
        }

        @Test
        void 해당_테마의_예약이_없으면_false를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            boolean result = reservationRepository.existsByThemeId(anotherTheme.getId());

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("id로 예약 존재 여부 조회")
    class ExistsById {

        @Test
        void 존재하는_id면_true를_반환한다() {
            Reservation saved = reservationRepository.save(
                    Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme)
            );

            boolean result = reservationRepository.existsById(saved.getId());

            assertThat(result).isTrue();
        }

        @Test
        void 존재하지_않는_id면_false를_반환한다() {
            boolean result = reservationRepository.existsById(999L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("인기 테마 id 조회")
    class FindThemeIdTop10 {

        @Test
        void 기간_내_예약이_많은_테마_id부터_최대_10개를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));
            reservationRepository.save(Reservation.of("유저2", LocalDate.of(2099, 12, 30), time, theme));
            reservationRepository.save(Reservation.of("유저3", LocalDate.of(2099, 12, 30), anotherTime, theme));
            reservationRepository.save(Reservation.of("다른유저", LocalDate.of(2099, 12, 31), time, anotherTheme));
            reservationRepository.save(Reservation.of("기간밖유저", LocalDate.of(2099, 12, 29), time, anotherTheme));

            List<Long> result = reservationRepository.findThemeIdTop10(
                    LocalDate.of(2099, 12, 30), LocalDate.of(2099, 12, 31)
            );

            assertThat(result).containsExactly(theme.getId(), anotherTheme.getId());
        }
    }

    @Nested
    @DisplayName("이름으로 예약 조회")
    class FindByName {

        @Test
        void 이름에_해당하는_예약을_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));
            reservationRepository.save(Reservation.of("유저2", LocalDate.of(2099, 12, 31), anotherTime, anotherTheme));

            List<Reservation> result = reservationRepository.findByName("유저1");

            assertAll(
                    () -> assertThat(result).hasSize(1),
                    () -> assertThat(result.get(0).getName()).isEqualTo("유저1"),
                    () -> assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2099, 12, 31)),
                    () -> assertThat(result.get(0).getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0)),
                    () -> assertThat(result.get(0).getTheme().getName()).isEqualTo("테마1")
            );
        }

        @Test
        void 이름에_해당하는_예약이_없으면_빈_리스트를_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            List<Reservation> result = reservationRepository.findByName("없는유저");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("id로 예약 조회")
    class FindById {

        @Test
        void 존재하는_id면_예약과_시간_테마를_함께_반환한다() {
            Reservation saved = reservationRepository.save(
                    Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme)
            );

            Optional<Reservation> result = reservationRepository.findById(saved.getId());

            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getName()).isEqualTo("유저1"),
                    () -> assertThat(result.get().getTime().getId()).isEqualTo(time.getId()),
                    () -> assertThat(result.get().getTime().getFinishAt()).isEqualTo(LocalTime.of(11, 0)),
                    () -> assertThat(result.get().getTheme().getId()).isEqualTo(theme.getId()),
                    () -> assertThat(result.get().getTheme().getDescription()).isEqualTo("설명1")
            );
        }

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환한다() {
            Optional<Reservation> result = reservationRepository.findById(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("id로 예약 존재 여부 조회 (FOR UPDATE)")
    class ExistsByIdForUpdate {

        @Test
        void 존재하는_id면_true를_반환한다() {
            Reservation saved = reservationRepository.save(
                    Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme)
            );

            boolean result = reservationRepository.existsByIdForUpdate(saved.getId());

            assertThat(result).isTrue();
        }

        @Test
        void 존재하지_않는_id면_false를_반환한다() {
            boolean result = reservationRepository.existsByIdForUpdate(999L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("날짜/시간/테마로 예약자 이름 조회 (FOR UPDATE)")
    class FindNameByDateAndTimeIdAndThemeIdForUpdate {

        @Test
        void 해당_날짜_시간_테마의_예약이_있으면_예약자_이름을_반환한다() {
            reservationRepository.save(Reservation.of("유저1", LocalDate.of(2099, 12, 31), time, theme));

            Optional<String> result = reservationRepository.findNameByDateAndTimeIdAndThemeId(
                    LocalDate.of(2099, 12, 31), time.getId(), theme.getId()
            );

            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get()).isEqualTo("유저1")
            );
        }

        @Test
        void 해당_날짜_시간_테마의_예약이_없으면_빈_Optional을_반환한다() {
            Optional<String> result = reservationRepository.findNameByDateAndTimeIdAndThemeId(
                    LocalDate.of(2099, 12, 31), time.getId(), theme.getId()
            );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("예약 날짜와 시간 수정")
    class UpdateDateAndTime {

        @Test
        void 날짜와_시간을_수정한다() {
            Reservation saved = reservationRepository.save(
                    Reservation.of("유저1", LocalDate.of(2099, 12, 30), time, theme)
            );

            reservationRepository.updateDateAndTime(saved.getId(), LocalDate.of(2099, 12, 31), anotherTime.getId());

            Optional<Reservation> result = reservationRepository.findById(saved.getId());
            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getDate()).isEqualTo(LocalDate.of(2099, 12, 31)),
                    () -> assertThat(result.get().getTime().getId()).isEqualTo(anotherTime.getId()),
                    () -> assertThat(result.get().getTime().getStartAt()).isEqualTo(LocalTime.of(12, 0))
            );
        }
    }
}
