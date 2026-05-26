package roomescape.domain.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.Status;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.JdbcThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.JdbcTimeRepository;
import roomescape.global.error.exception.GeneralException;

class JdbcReservationRepositoryTest {

    private JdbcReservationRepository reservationRepository;
    private JdbcTimeRepository timeRepository;
    private JdbcThemeRepository themeRepository;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);

        reservationRepository = new JdbcReservationRepository(dataSource);
        timeRepository = new JdbcTimeRepository(dataSource);
        themeRepository = new JdbcThemeRepository(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    class SaveTest {

        @Test
        void 성공() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = Reservation.create("예약자", LocalDate.of(2026, 5, 1), time, theme);

            // when
            Reservation actual = reservationRepository.save(reservation);

            // then
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getName()).isEqualTo("예약자");
            assertThat(actual.getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(actual.getTime().getId()).isEqualTo(time.getId());
            assertThat(actual.getTheme().getId()).isEqualTo(theme.getId());
        }

        @Test
        void 삭제된_예약과_같은_날짜_시간_테마로_다시_저장할_수_있다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            LocalDate date = LocalDate.of(2026, 5, 1);
            Reservation deletedReservation = reservationRepository.save(Reservation.create("예약자1", date, time, theme));
            reservationRepository.deleteReservationById(deletedReservation.getId());

            // when
            Reservation actual = reservationRepository.save(Reservation.create("예약자2", date, time, theme));

            // then
            assertThat(reservationRepository.findReservationsByDeletedAtIsNull())
                .extracting(Reservation::getId, Reservation::getName)
                .containsExactly(tuple(actual.getId(), "예약자2"));
        }

        @Test
        void 삭제되지_않고_취소되지_않은_예약은_같은_날짜_시간_테마로_중복_저장할_수_없다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            LocalDate date = LocalDate.of(2026, 5, 1);
            reservationRepository.save(Reservation.create("예약자1", date, time, theme));

            // when & then
            assertThatThrownBy(() -> reservationRepository.save(Reservation.create("예약자2", date, time, theme)))
                .isInstanceOf(DuplicateKeyException.class);
        }
    }

    @Nested
    class FindAllReservationsTest {

        @Test
        void 시간과_테마를_JOIN해서_예약을_조회한다() {
            // given
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Reservation reservation1 = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time1, theme1));
            Reservation reservation2 = reservationRepository.save(
                Reservation.create("예약자2", LocalDate.of(2026, 5, 2), time2, theme2));

            // when
            List<Reservation> actual = reservationRepository.findReservationsByDeletedAtIsNull();

            // then
            assertThat(actual)
                .extracting(
                    Reservation::getId,
                    Reservation::getName,
                    Reservation::getDate,
                    reservation -> reservation.getTime().getStartAt(),
                    reservation -> reservation.getTheme().getName()
                )
                .containsExactly(
                    tuple(reservation1.getId(), "예약자1", LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), "테마1"),
                    tuple(reservation2.getId(), "예약자2", LocalDate.of(2026, 5, 2), LocalTime.of(11, 0), "테마2")
                );
        }

        @Test
        void 삭제된_시간과_테마에_연결된_예약도_삭제되지_않았으면_조회한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            timeRepository.deleteTimeById(time.getId());
            themeRepository.deleteThemeById(theme.getId());

            // when
            List<Reservation> actual = reservationRepository.findReservationsByDeletedAtIsNull();

            // then
            assertThat(actual)
                .extracting(
                    Reservation::getId,
                    Reservation::getName,
                    reservationResult -> reservationResult.getTime().getStartAt(),
                    reservationResult -> reservationResult.getTheme().getName()
                )
                .containsExactly(tuple(reservation.getId(), "예약자1", LocalTime.of(10, 0), "테마1"));
        }
    }

    @Nested
    class FindReservationsByNameTest {

        @Test
        void 이름이_일치하는_삭제되지_않은_예약만_조회한다() {
            // given
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation1 = reservationRepository.save(
                Reservation.create("브라운", LocalDate.of(2026, 5, 1), time1, theme));
            Reservation deletedReservation = reservationRepository.save(
                Reservation.create("브라운", LocalDate.of(2026, 5, 2), time2, theme));
            reservationRepository.save(Reservation.create("제이슨", LocalDate.of(2026, 5, 3), time3, theme));
            reservationRepository.deleteReservationById(deletedReservation.getId());

            // when
            List<Reservation> actual = reservationRepository.findReservationsByNameAndDeletedAtIsNull("브라운");

            // then
            assertThat(actual)
                .extracting(Reservation::getId, Reservation::getName, Reservation::getDate)
                .containsExactly(tuple(reservation1.getId(), "브라운", LocalDate.of(2026, 5, 1)));
        }

        @Test
        void 이름이_일치하는_예약이_없으면_빈_목록을_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            reservationRepository.save(Reservation.create("브라운", LocalDate.of(2026, 5, 1), time, theme));

            // when
            List<Reservation> actual = reservationRepository.findReservationsByNameAndDeletedAtIsNull("제이슨");

            // then
            assertThat(actual).isEmpty();
        }

        @Test
        void 삭제된_시간과_테마에_연결된_예약의_삭제_시각도_조회한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            reservationRepository.save(Reservation.create("브라운", LocalDate.of(2026, 5, 1), time, theme));
            timeRepository.deleteTimeById(time.getId());
            themeRepository.deleteThemeById(theme.getId());

            // when
            List<Reservation> actual = reservationRepository.findReservationsByNameAndDeletedAtIsNull("브라운");

            // then
            assertThat(actual).hasSize(1);
            assertThat(actual.get(0).getTime().getDeletedAt()).isNotNull();
            assertThat(actual.get(0).getTheme().getDeletedAt()).isNotNull();
        }

        @Test
        void 날짜순_시간순으로_조회한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation1 = reservationRepository.save(Reservation.create("브라운", date, time2, theme));
            Reservation reservation2 = reservationRepository.save(Reservation.create("브라운", date, time1, theme));
            Reservation reservation3 = reservationRepository.save(
                Reservation.create("브라운", date.minusDays(1), time3, theme));

            // when
            List<Reservation> actual = reservationRepository.findReservationsByNameAndDeletedAtIsNull("브라운");

            // then
            assertThat(actual)
                .extracting(Reservation::getId)
                .containsExactly(reservation3.getId(), reservation2.getId(), reservation1.getId());
        }
    }

    @Nested
    class FindTimeIdsByDateAndThemeIdTest {

        @Test
        void 같은_날짜와_테마에_예약된_시간_ID만_조회한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
            Time deletedTime = timeRepository.save(Time.create(LocalTime.of(13, 0)));
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            reservationRepository.save(Reservation.create("예약자1", date, time1, theme1));
            Reservation deletedReservation = reservationRepository.save(
                Reservation.create("삭제된예약자", date, time2, theme1));
            Reservation canceledReservation = reservationRepository.save(
                Reservation.create("취소된예약자", date, time3, theme1));
            reservationRepository.save(Reservation.create("예약자2", date, time2, theme2));
            reservationRepository.save(Reservation.create("예약자3", date.plusDays(1), time2, theme1));
            reservationRepository.save(Reservation.create("삭제된시간예약자", date, deletedTime, theme1));
            reservationRepository.deleteReservationById(deletedReservation.getId());
            reservationRepository.update(canceledReservation.cancel());
            timeRepository.deleteTimeById(deletedTime.getId());

            // when
            List<Long> actual = reservationRepository.findTimeIdsByDateAndThemeIdAndDeletedAtIsNull(date,
                theme1.getId());

            // then
            assertThat(actual).containsExactly(time1.getId());
        }
    }

    @Nested
    class ExistsReservationByDateAndTimeAndThemeTest {

        @Test
        void 같은_날짜_시간_테마의_예약이_있으면_true를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            reservationRepository.save(Reservation.create("예약자", date, time, theme));

            // when
            boolean actual = reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNull(date, time,
                theme);

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 삭제된_예약은_false를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(Reservation.create("예약자", date, time, theme));
            reservationRepository.deleteReservationById(reservation.getId());

            // when
            boolean actual = reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNull(date, time,
                theme);

            // then
            assertThat(actual).isFalse();
        }

        @Test
        void 취소된_예약은_false를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(Reservation.create("예약자", date, time, theme));
            reservationRepository.update(reservation.cancel());

            // when
            boolean actual = reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNull(date, time,
                theme);

            // then
            assertThat(actual).isFalse();
        }

        @Test
        void 같은_날짜_시간_테마의_예약이_없으면_false를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

            // when
            boolean actual = reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNull(date, time,
                theme);

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class UpdateTest {

        @Test
        void 성공() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time updateTime = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme updateTheme = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            Reservation updateReservation = Reservation.reconstruct(reservation.getId(), "예약자2",
                LocalDate.of(2026, 5, 2), updateTime, updateTheme, Status.ACTIVE);

            // when
            Reservation actual = reservationRepository.update(updateReservation);

            // then
            assertThat(actual.getId()).isEqualTo(reservation.getId());
            assertThat(reservationRepository.findReservationByIdAndDeletedAtIsNull(reservation.getId()))
                .get()
                .extracting(
                    Reservation::getName,
                    Reservation::getDate,
                    result -> result.getTime().getId(),
                    result -> result.getTheme().getId()
                )
                .containsExactly("예약자2", LocalDate.of(2026, 5, 2), updateTime.getId(), updateTheme.getId());
        }

        @Test
        void 이미_취소된_예약이면_예외가_발생한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time updateTime = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme updateTheme = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            reservationRepository.update(reservation.cancel());
            Reservation updateReservation = Reservation.reconstruct(reservation.getId(), "예약자2",
                LocalDate.of(2026, 5, 2), updateTime, updateTheme, Status.ACTIVE);

            // when & then
            assertThatThrownBy(() -> reservationRepository.update(updateReservation))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
        }

        @Test
        void 수정하려는_예약이_이미_삭제되었으면_예외가_발생한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            reservationRepository.deleteReservationById(reservation.getId());

            // when & then
            assertThatThrownBy(() -> reservationRepository.update(reservation))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
        }
    }

    @Nested
    class ExistsReservationByDateAndTimeAndThemeAndIdNotTest {

        @Test
        void 자기_자신_예약은_false를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(Reservation.create("예약자", date, time, theme));

            // when
            boolean actual = reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNullAndIdNot(
                date, time, theme, reservation.getId());

            // then
            assertThat(actual).isFalse();
        }

        @Test
        void 다른_예약이_있으면_true를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            reservationRepository.save(Reservation.create("예약자1", date, time, theme));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자2", date.plusDays(1), time, theme));

            // when
            boolean actual = reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNullAndIdNot(
                date, time, theme, reservation.getId());

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 다른_예약이_취소된_예약이면_false를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation canceledReservation = reservationRepository.save(Reservation.create("예약자1", date, time, theme));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자2", date.plusDays(1), time, theme));
            reservationRepository.update(canceledReservation.cancel());

            // when
            boolean actual = reservationRepository.existsReservationByDateAndTimeAndThemeAndDeletedAtIsNullAndIdNot(
                date, time, theme, reservation.getId());

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class DeleteReservationByIdTest {

        @Test
        void 성공() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation1 = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            Reservation reservation2 = reservationRepository.save(
                Reservation.create("예약자2", LocalDate.of(2026, 5, 2), time, theme));

            // when
            reservationRepository.deleteReservationById(reservation1.getId());

            // then
            assertThat(reservationRepository.findReservationsByDeletedAtIsNull())
                .extracting(Reservation::getId)
                .containsExactly(reservation2.getId());
            assertThat(countDeletedReservationById(reservation1.getId())).isEqualTo(1);
            assertThat(reservationRepository.existsReservationByIdAndDeletedAtIsNull(reservation1.getId())).isFalse();
        }

        @Test
        void 삭제하려는_예약이_이미_삭제되었으면_예외가_발생한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            reservationRepository.deleteReservationById(reservation.getId());

            // when & then
            assertThatThrownBy(() -> reservationRepository.deleteReservationById(reservation.getId()))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
        }
    }

    private Integer countDeletedReservationById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reservation WHERE id = ? AND status = 'DELETED'",
            Integer.class,
            id
        );
    }

}
