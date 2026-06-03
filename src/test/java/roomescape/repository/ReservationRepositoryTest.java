package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationTimesWithStatus;

@JdbcTest
@Import(ReservationRepository.class)
@Sql(scripts = "/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired ReservationRepository reservationRepository;

    private Theme theme;
    private ReservationTime reservationTime;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테스트 테마", "테스트용 테마 설명입니다.", "https://img.com");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)",
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        theme = Theme.createWithId(1L, "테스트 테마", "테스트용 테마 설명입니다.", "https://img.com");
        reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    @Nested
    class save {

        @Test
        void 저장_후_id가_부여된_객체를_반환() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Reservation reservation = Reservation.create("검프", date, reservationTime, theme);

            // when
            Reservation saved = reservationRepository.save(reservation);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("검프");
            assertThat(saved.getReservationDate().getDate()).isEqualTo(date);
        }
    }

    @Nested
    class findAll {

        @Test
        void 저장된_모든_예약을_반환() {
            // given
            reservationRepository.save(Reservation.create("검프", LocalDate.now().plusDays(1), reservationTime, theme));
            reservationRepository.save(Reservation.create("리오", LocalDate.now().plusDays(2), reservationTime, theme));

            // when
            List<Reservation> result = reservationRepository.findAll();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        void 저장된_예약이_없으면_빈_목록을_반환() {
            // when
            List<Reservation> result = reservationRepository.findAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findById {

        @Test
        void 존재하는_id면_예약을_반환() {
            // given
            Reservation saved = reservationRepository.save(Reservation.create("검프", LocalDate.now().plusDays(1), reservationTime, theme));

            // when
            Optional<Reservation> result = reservationRepository.findById(saved.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("검프");
        }

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환() {
            // when
            Optional<Reservation> result = reservationRepository.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findByName {

        @Test
        void 이름으로_예약_목록을_반환() {
            // given
            reservationRepository.save(Reservation.create("검프", LocalDate.now().plusDays(1), reservationTime, theme));
            reservationRepository.save(Reservation.create("검프", LocalDate.now().plusDays(2), reservationTime, theme));
            reservationRepository.save(Reservation.create("리오", LocalDate.now().plusDays(3), reservationTime, theme));

            // when
            List<Reservation> result = reservationRepository.findByName("검프");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(r -> r.getName().equals("검프"));
        }

        @Test
        void 없는_이름이면_빈_목록을_반환() {
            // when
            List<Reservation> result = reservationRepository.findByName("없는사람");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class existsByDateAndTimeIdAndThemeId {

        @Test
        void 동일한_조건의_예약이_있으면_true를_반환() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            reservationRepository.save(Reservation.create("검프", date, reservationTime, theme));

            // when
            boolean result = reservationRepository.existsByDateAndTimeIdAndThemeId(date, 1L, 1L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 동일한_조건의_예약이_없으면_false를_반환() {
            // when
            boolean result = reservationRepository.existsByDateAndTimeIdAndThemeId(LocalDate.now().plusDays(1), 1L, 1L);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class existsByTimeId {

        @Test
        void 해당_시간의_예약이_있으면_true를_반환() {
            // given
            reservationRepository.save(Reservation.create("검프", LocalDate.now().plusDays(1), reservationTime, theme));

            // when
            boolean result = reservationRepository.existsByTimeId(1L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 해당_시간의_예약이_없으면_false를_반환() {
            // when
            boolean result = reservationRepository.existsByTimeId(1L);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class existsByThemeId {

        @Test
        void 해당_테마의_예약이_있으면_true를_반환() {
            // given
            reservationRepository.save(Reservation.create("검프", LocalDate.now().plusDays(1), reservationTime, theme));

            // when
            boolean result = reservationRepository.existsByThemeId(1L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 해당_테마의_예약이_없으면_false를_반환() {
            // when
            boolean result = reservationRepository.existsByThemeId(1L);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class updateDateAndTime {

        @Test
        void 날짜와_시간이_변경된다() {
            // given
            Reservation saved = reservationRepository.save(Reservation.create("검프", LocalDate.now().plusDays(1), reservationTime, theme));

            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)",
                    LocalTime.of(14, 0), LocalTime.of(15, 0));
            ReservationTime newTime = ReservationTime.createWithId(2L, LocalTime.of(14, 0), LocalTime.of(15, 0));
            LocalDate newDate = LocalDate.now().plusDays(3);

            Reservation modified = saved.modify(newDate, newTime, theme);

            // when
            reservationRepository.updateDateAndTime(modified);

            // then
            Reservation found = reservationRepository.findById(saved.getId()).get();
            assertThat(found.getReservationDate().getDate()).isEqualTo(newDate);
            assertThat(found.getTime().getId()).isEqualTo(2L);
        }
    }

    @Nested
    class deleteById {

        @Test
        void 예약을_삭제한다() {
            // given
            Reservation saved = reservationRepository.save(Reservation.create("검프", LocalDate.now().plusDays(1), reservationTime, theme));

            // when
            reservationRepository.deleteById(saved.getId());

            // then
            assertThat(reservationRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    class findReservationTimeStatusesByDateAndThemeId {

        @Test
        void 예약된_시간은_reserved가_true() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            reservationRepository.save(Reservation.create("검프", date, reservationTime, theme));

            // when
            List<ReservationTimesWithStatus> result = reservationRepository.findReservationTimeStatusesByDateAndThemeId(date, 1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().reserved()).isTrue();
        }

        @Test
        void 예약되지_않은_시간은_reserved가_false() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);

            // when
            List<ReservationTimesWithStatus> result = reservationRepository.findReservationTimeStatusesByDateAndThemeId(date, 1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().reserved()).isFalse();
        }
    }
}
