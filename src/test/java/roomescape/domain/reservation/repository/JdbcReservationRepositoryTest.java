package roomescape.domain.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.vo.ReservationSchedule;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.JdbcThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.JdbcTimeRepository;
import roomescape.global.error.exception.GeneralException;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class JdbcReservationRepositoryTest {

    private static volatile boolean saveSucceeded = false;
    private static volatile boolean findSucceeded = false;

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
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class 예약_저장 {

        @Test
        @Order(1)
        void 예약을_저장한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = Reservation.create(
                "예약자", LocalDate.of(2026, 5, 1), time, theme);

            // when
            Reservation actual = reservationRepository.save(reservation);

            // then
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getName()).isEqualTo("예약자");
            assertThat(actual.getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(actual.getTime().getId()).isEqualTo(time.getId());
            assertThat(actual.getTheme().getId()).isEqualTo(theme.getId());
            saveSucceeded = true;
        }

        @Test
        @Order(2)
        void 삭제된_예약과_같은_날짜_시간_테마로_다시_저장할_수_있다() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");

            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            LocalDate date = LocalDate.of(2026, 5, 1);
            Reservation deleted = reservationRepository.save(
                Reservation.create("예약자1", date, time, theme));
            reservationRepository.deleteReservationById(deleted.getId());

            // when
            Reservation actual = reservationRepository.save(
                Reservation.create("예약자2", date, time, theme));

            // then
            assertThat(findReservationsByNotDeleted())
                .extracting(
                    Reservation::getId,
                    reservation -> reservation.getName()
                )
                .containsExactly(tuple(actual.getId(), "예약자2"));
        }

        @Test
        @Order(3)
        void 삭제되지_않은_같은_날짜_시간_테마의_예약은_중복_저장할_수_없다() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");

            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            LocalDate date = LocalDate.of(2026, 5, 1);
            reservationRepository.save(Reservation.create("예약자1", date, time, theme));

            // when & then
            assertThatThrownBy(() -> reservationRepository.save(
                Reservation.create("예약자2", date, time, theme)))
                .isInstanceOf(DuplicateKeyException.class);
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class 예약_목록_조회 {

        @BeforeEach
        void assumeSaveWorks() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        @Order(1)
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
            List<Reservation> actual = findReservationsByNotDeleted();

            // then
            assertThat(actual)
                .extracting(
                    Reservation::getId,
                    reservation -> reservation.getName(),
                    Reservation::getDate,
                    reservation -> reservation.getTime().getStartAt(),
                    reservation -> reservation.getTheme().getName()
                )
                .containsExactly(
                    tuple(reservation1.getId(), "예약자1", LocalDate.of(2026, 5, 1), LocalTime.of(10, 0), "테마1"),
                    tuple(reservation2.getId(), "예약자2", LocalDate.of(2026, 5, 2), LocalTime.of(11, 0), "테마2")
                );
            findSucceeded = true;
        }

        @Test
        @Order(2)
        void 삭제된_시간과_테마에_연결된_예약도_삭제되지_않았으면_조회한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            timeRepository.deleteTimeById(time.getId());
            themeRepository.deleteThemeById(theme.getId());

            // when
            List<Reservation> actual = findReservationsByNotDeleted();

            // then
            assertThat(actual)
                .extracting(
                    Reservation::getId,
                    r -> r.getName(),
                    r -> r.getTime().getStartAt(),
                    r -> r.getTheme().getName()
                )
                .containsExactly(tuple(reservation.getId(), "예약자1", LocalTime.of(10, 0), "테마1"));
        }
    }

    @Nested
    class 이름으로_예약_조회 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

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
            reservationRepository.save(
                Reservation.create("제이슨", LocalDate.of(2026, 5, 3), time3, theme));
            reservationRepository.deleteReservationById(deletedReservation.getId());

            // when
            List<ReservationWithWaitingNumber> actual =
                reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("브라운");

            // then
            assertThat(actual)
                .extracting(
                    reservationWithWaitingNumber -> reservationWithWaitingNumber.reservation().getId(),
                    reservationWithWaitingNumber -> reservationWithWaitingNumber.reservation().getName(),
                    reservationWithWaitingNumber -> reservationWithWaitingNumber.reservation().getDate()
                )
                .containsExactly(tuple(reservation1.getId(), "브라운", LocalDate.of(2026, 5, 1)));
        }

        @Test
        void 이름이_일치하는_예약이_없으면_빈_목록을_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            reservationRepository.save(
                Reservation.create("브라운", LocalDate.of(2026, 5, 1), time, theme));

            // when
            List<ReservationWithWaitingNumber> actual =
                reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("제이슨");

            // then
            assertThat(actual).isEmpty();
        }

        @Test
        void 삭제된_시간과_테마에_연결된_예약의_삭제_시각도_조회한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            reservationRepository.save(
                Reservation.create("브라운", LocalDate.of(2026, 5, 1), time, theme));
            timeRepository.deleteTimeById(time.getId());
            themeRepository.deleteThemeById(theme.getId());

            // when
            List<ReservationWithWaitingNumber> actual =
                reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("브라운");

            // then
            assertThat(actual).hasSize(1);
            assertThat(actual.getFirst().reservation().getTime().isDeleted()).isTrue();
            assertThat(actual.getFirst().reservation().getTheme().isDeleted()).isTrue();
        }

        @Test
        void 날짜순_시간순으로_조회한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation1 = reservationRepository.save(
                Reservation.create("브라운", date, time2, theme));
            Reservation reservation2 = reservationRepository.save(
                Reservation.create("브라운", date, time1, theme));
            Reservation reservation3 = reservationRepository.save(
                Reservation.create("브라운", date.minusDays(1), time3, theme));

            // when
            List<ReservationWithWaitingNumber> actual =
                reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("브라운");

            // then
            assertThat(actual)
                .extracting(reservationWithWaitingNumber -> reservationWithWaitingNumber.reservation().getId())
                .containsExactly(reservation3.getId(), reservation2.getId(), reservation1.getId());
        }
    }

    @Nested
    class 날짜와_테마로_예약된_시간_조회 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 같은_날짜와_테마에_활성_예약된_시간_ID만_조회한다() {
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
            reservationRepository.save(
                Reservation.create("삭제된시간예약자", date, deletedTime, theme1));
            reservationRepository.deleteReservationById(deletedReservation.getId());
            reservationRepository.update(canceledReservation.cancel());
            timeRepository.deleteTimeById(deletedTime.getId());

            // when
            List<Long> actual = reservationRepository.findTimeIdsByDateAndThemeIdAndNotDeleted(
                date, theme1.getId());

            // then
            assertThat(actual).containsExactly(time1.getId());
        }
    }

    @Nested
    class 예약_대기_순번_조회 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 예약_목록을_조회할_때_대기_순번을_함께_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation active = reservationRepository.save(
                Reservation.create("활성예약자", date, time, theme));
            Reservation firstWaiting = reservationRepository.save(
                Reservation.create("대기예약자1", date, time, theme).toWaiting());
            Reservation canceledWaiting = reservationRepository.save(
                Reservation.create("대기예약자2", date, time, theme).toWaiting());
            reservationRepository.update(canceledWaiting.cancel());
            Reservation secondWaiting = reservationRepository.save(
                Reservation.create("대기예약자3", date, time, theme).toWaiting());

            // when
            List<ReservationWithWaitingNumber> actual =
                reservationRepository.findReservationsByNotDeletedWithWaitingNumber();

            // then
            assertThat(actual)
                .extracting(
                    reservation -> reservation.reservation().getId(),
                    ReservationWithWaitingNumber::waitingNumber
                )
                .containsExactly(
                    tuple(active.getId(), null),
                    tuple(firstWaiting.getId(), 1),
                    tuple(canceledWaiting.getId(), null),
                    tuple(secondWaiting.getId(), 2)
                );
        }

        @Test
        void 이름으로_조회해도_전체_대기열_기준의_순번을_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            reservationRepository.save(Reservation.create("제이슨", date, time, theme).toWaiting());
            Reservation brown = reservationRepository.save(
                Reservation.create("브라운", date, time, theme).toWaiting());

            // when
            List<ReservationWithWaitingNumber> actual =
                reservationRepository.findReservationsByNameAndNotDeletedWithWaitingNumber("브라운");

            // then
            assertThat(actual)
                .extracting(
                    reservation -> reservation.reservation().getId(),
                    ReservationWithWaitingNumber::waitingNumber
                )
                .containsExactly(tuple(brown.getId(), 2));
        }
    }

    @Nested
    class 예약_수정 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 예약을_수정한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time updateTime = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme updateTheme = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            Reservation updateReservation = Reservation.reconstruct(reservation.getId(), "예약자2",
                LocalDate.of(2026, 5, 2), updateTime, updateTheme, ReservationStatus.ACTIVE);

            // when
            Reservation actual = reservationRepository.update(updateReservation);

            // then
            assertThat(actual.getId()).isEqualTo(reservation.getId());
            assertThat(reservationRepository.findReservationByIdAndNotDeleted(reservation.getId()))
                .get()
                .extracting(
                    r -> r.getName(),
                    Reservation::getDate,
                    r -> r.getTime().getId(),
                    r -> r.getTheme().getId()
                )
                .containsExactly("예약자2", LocalDate.of(2026, 5, 2), updateTime.getId(), updateTheme.getId());
        }

        @Test
        void 이미_수정된_예약을_이전_버전으로_수정하면_예외가_발생한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time updateTime = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme updateTheme = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            Reservation updateReservation = Reservation.reconstruct(reservation.getId(), "예약자2",
                LocalDate.of(2026, 5, 2), updateTime, updateTheme, ReservationStatus.ACTIVE);
            reservationRepository.update(updateReservation);

            // when & then
            assertThatThrownBy(() -> reservationRepository.update(updateReservation))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약 정보가 이미 수정되었습니다. 다시 조회 후 시도해주세요.");
        }

        @Test
        void 삭제된_예약을_수정하면_예약_없음_예외가_발생한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            Reservation updateReservation = Reservation.reconstruct(reservation.getId(), "예약자2",
                LocalDate.of(2026, 5, 2), time, theme, ReservationStatus.ACTIVE);
            reservationRepository.deleteReservationById(reservation.getId());

            // when & then
            assertThatThrownBy(() -> reservationRepository.update(updateReservation))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");
        }

        @Test
        void 취소된_예약을_수정하면_이미_취소됨_예외가_발생한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자1", LocalDate.of(2026, 5, 1), time, theme));
            Reservation canceled = reservationRepository.update(reservation.cancel());
            Reservation updateReservation = Reservation.reconstruct(canceled.getId(), "예약자2",
                LocalDate.of(2026, 5, 2), time, theme, ReservationStatus.ACTIVE, canceled.getVersion());

            // when & then
            assertThatThrownBy(() -> reservationRepository.update(updateReservation))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 취소된 예약입니다.");
        }
    }

    @Nested
    class 예약_상태_존재_여부_확인 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 예약_속성과_상태가_일치하는_예약이_있으면_true를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create("예약자", date, time, theme));

            // when
            boolean actual = reservationRepository.existsReservationAndStatus(reservation, reservation.getStatus());

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 상태가_다르면_false를_반환한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation activeReservation = reservationRepository.save(
                Reservation.create("예약자", date, time, theme));

            // when
            boolean actual = reservationRepository.existsReservationAndStatus(
                activeReservation, ReservationStatus.WAITING);

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class 예약_삭제 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 예약을_소프트_삭제한다() {
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
            assertThat(findReservationsByNotDeleted())
                .extracting(Reservation::getId)
                .containsExactly(reservation2.getId());
            assertThat(countDeletedReservationById(reservation1.getId())).isEqualTo(1);
            assertThat(reservationRepository.existsReservationByIdAndNotDeleted(reservation1.getId())).isFalse();
        }

        @Test
        void 이미_삭제된_예약을_삭제하면_예외가_발생한다() {
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

    @Nested
    class 날짜_테마_시간으로_예약_조회 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 날짜_테마_시간에_활성_예약이_존재하는지_확인한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time targetTime = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time otherTime = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme targetTheme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme otherTheme = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            reservationRepository.save(Reservation.create("예약자1", date, targetTime, targetTheme));
            Reservation canceledReservation = reservationRepository.save(
                Reservation.create("취소자1", date, targetTime, targetTheme).toWaiting());
            reservationRepository.update(canceledReservation.cancel());
            reservationRepository.save(
                Reservation.create("다른날짜", date.plusDays(1), targetTime, targetTheme));
            reservationRepository.save(
                Reservation.create("다른시간", date, otherTime, targetTheme));
            reservationRepository.save(
                Reservation.create("다른테마", date, targetTime, otherTheme));

            // when
            boolean actual = reservationRepository.existsActiveReservationBySchedule(
                new ReservationSchedule(date, targetTheme.getId(), targetTime.getId()));
            boolean notFound = reservationRepository.existsActiveReservationBySchedule(
                new ReservationSchedule(date.minusDays(1), targetTheme.getId(), targetTime.getId()));

            // then
            assertThat(actual).isTrue();
            assertThat(notFound).isFalse();
        }

        @Test
        void 날짜_테마_시간에_활성_예약을_락으로_조회한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time targetTime = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time otherTime = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme targetTheme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme otherTheme = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Reservation active = reservationRepository.save(
                Reservation.create("예약자1", date, targetTime, targetTheme));
            Reservation canceledReservation = reservationRepository.save(
                Reservation.create("취소자1", date.plusDays(1), targetTime, targetTheme));
            reservationRepository.update(canceledReservation.cancel());
            reservationRepository.save(
                Reservation.create("다른시간", date, otherTime, targetTheme));
            reservationRepository.save(
                Reservation.create("다른테마", date, targetTime, otherTheme));

            // when
            Optional<Long> actual =
                reservationRepository.lockActiveReservationBySchedule(
                    new ReservationSchedule(date, targetTheme.getId(), targetTime.getId()));
            Optional<Long> notFound =
                reservationRepository.lockActiveReservationBySchedule(
                    new ReservationSchedule(date.minusDays(1), targetTheme.getId(), targetTime.getId()));

            // then
            assertThat(actual).contains(active.getId());
            assertThat(notFound).isEmpty();
        }

        @Test
        void 날짜_테마_시간에_가장_먼저_생성된_대기_예약을_조회한다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time targetTime = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time otherTime = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme targetTheme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme otherTheme = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Reservation canceledWaiting = reservationRepository.save(
                Reservation.create("취소대기자", date, targetTime, targetTheme).toWaiting());
            reservationRepository.update(canceledWaiting.cancel());
            Reservation firstWaiting = reservationRepository.save(
                Reservation.create("대기자1", date, targetTime, targetTheme).toWaiting());
            reservationRepository.save(
                Reservation.create("대기자2", date, targetTime, targetTheme).toWaiting());
            reservationRepository.save(
                Reservation.create("다른날짜", date.plusDays(1), targetTime, targetTheme).toWaiting());
            reservationRepository.save(
                Reservation.create("다른시간", date, otherTime, targetTheme).toWaiting());
            reservationRepository.save(
                Reservation.create("다른테마", date, targetTime, otherTheme).toWaiting());

            // when
            Optional<Reservation> actual =
                reservationRepository.lockFirstWaitingReservationBySchedule(
                    new ReservationSchedule(date, targetTheme.getId(), targetTime.getId()));

            // then
            assertThat(actual).map(Reservation::getId).contains(firstWaiting.getId());
        }

        @Test
        void 삭제된_대기_예약은_첫_번째_대기_예약으로_조회_되면_안된다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Reservation deletedReservation = reservationRepository.save(
                Reservation.create("삭제된대기자", date, time, theme).toWaiting());
            reservationRepository.deleteReservationById(deletedReservation.getId());

            // when
            Optional<Reservation> actual =
                reservationRepository.lockFirstWaitingReservationBySchedule(
                    new ReservationSchedule(date, theme.getId(), time.getId()));

            // then
            assertThat(actual).isEmpty();
        }
    }

    private Integer countDeletedReservationById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reservation WHERE id = ? AND deleted_at IS NOT NULL",
            Integer.class,
            id
        );
    }

    private List<Reservation> findReservationsByNotDeleted() {
        return reservationRepository.findReservationsByNotDeletedWithWaitingNumber()
            .stream()
            .map(ReservationWithWaitingNumber::reservation)
            .toList();
    }
}
