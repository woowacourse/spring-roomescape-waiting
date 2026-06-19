package roomescape.feature.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.domain.SlotKey;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.repository.JdbcThemeRepository;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.repository.JdbcTimeRepository;
import roomescape.fixture.ReservationFixture;
import roomescape.global.domain.EntityStatus;

@JdbcTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class JdbcReservationRepositoryTest {

    private static volatile boolean saveSucceeded = false;
    private static volatile boolean findSucceeded = false;

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcReservationRepository reservationRepository;
    private JdbcTimeRepository timeRepository;
    private JdbcThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new JdbcReservationRepository(dataSource);
        timeRepository = new JdbcTimeRepository(dataSource);
        themeRepository = new JdbcThemeRepository(dataSource);
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
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            ReserverName name = new ReserverName("예약자");
            LocalDate date = LocalDate.now().plusYears(1);
            Reservation reservation = Reservation.create(name, date, time, theme, ReservationStatus.ACTIVE, 1_000L);

            // when
            Long savedReservationId = reservationRepository.save(reservation).getId();

            Reservation expectedSavedReservation = Reservation.reconstruct(
                savedReservationId, name, date, time, theme, ReservationStatus.ACTIVE);

            // then
            Reservation actualSavedReservation = jdbcTemplate.queryForObject(
                "SELECT id, name, date, time_id, theme_id, status FROM reservation WHERE id = ?",
                (rs, rowNum) -> Reservation.reconstruct(
                    rs.getLong("id"),
                    new ReserverName(rs.getString("name")),
                    rs.getDate("date").toLocalDate(),
                    Time.reconstruct(rs.getLong("time_id"), time.getStartAt(), EntityStatus.ACTIVE),
                    Theme.reconstruct(rs.getLong("theme_id"), theme.getName(), theme.getDescription(), theme.getImageUrl(), EntityStatus.ACTIVE),
                    ReservationStatus.valueOf(rs.getString("status"))
                ),
                savedReservationId
            );

            assertThat(actualSavedReservation).usingRecursiveComparison()
                .isEqualTo(expectedSavedReservation);

            saveSucceeded = true;
        }

        @Test
        @Order(2)
        void 삭제된_예약과_같은_날짜_시간_테마로_다시_저장할_수_있다() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");

            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            Reservation deleted = reservationRepository.save(
                Reservation.create(new ReserverName("예약자1"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.update(deleted.delete());

            // when
            Reservation actual = reservationRepository.save(
                Reservation.create(new ReserverName("예약자2"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));

            // then
            assertThat(reservationRepository.findAllReservations())
                .extracting(r -> r.getName().value(), Reservation::getStatus)
                .containsExactlyInAnyOrder(
                    tuple("예약자1", ReservationStatus.DELETED),
                    tuple("예약자2", ReservationStatus.ACTIVE)
                );
            assertThat(actual.getName().value()).isEqualTo("예약자2");
        }

        @Test
        @Order(3)
        void 삭제되지_않은_같은_날짜_시간_테마의_예약은_중복_저장할_수_없다() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");

            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            reservationRepository.save(Reservation.create(new ReserverName("예약자1"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));

            // when & then
            assertThatThrownBy(() -> reservationRepository.save(
                Reservation.create(new ReserverName("예약자2"), date, time, theme, ReservationStatus.ACTIVE, 1_000L)))
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
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "https://example.com/image2.png"));
            LocalDate date1 = LocalDate.now().plusYears(1);
            LocalDate date2 = LocalDate.now().plusYears(1).plusDays(1);
            Reservation reservation1 = reservationRepository.save(
                Reservation.create(new ReserverName("예약자1"), date1, time1, theme1, ReservationStatus.ACTIVE, 1_000L));
            Reservation reservation2 = reservationRepository.save(
                Reservation.create(new ReserverName("예약자2"), date2, time2, theme2, ReservationStatus.ACTIVE, 1_000L));

            // when
            List<Reservation> actual = reservationRepository.findAllReservations();

            // then
            assertThat(actual)
                .extracting(
                    Reservation::getId,
                    r -> r.getName().value(),
                    Reservation::getDate,
                    r -> r.getTime().getStartAt(),
                    r -> r.getTheme().getName()
                )
                .containsExactly(
                    tuple(reservation1.getId(), "예약자1", date1, LocalTime.of(10, 0), "테마1"),
                    tuple(reservation2.getId(), "예약자2", date2, LocalTime.of(11, 0), "테마2")
                );
            findSucceeded = true;
        }

        @Test
        @Order(2)
        void 삭제된_시간과_테마에_연결된_예약도_삭제되지_않았으면_조회한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation reservation = reservationRepository.save(
                Reservation.create(new ReserverName("예약자1"), LocalDate.now().plusYears(1), time, theme, ReservationStatus.ACTIVE, 1_000L));
            timeRepository.deleteTimeById(time.getId());
            themeRepository.deleteThemeById(theme.getId());

            // when
            List<Reservation> actual = reservationRepository.findAllReservations();

            // then
            assertThat(actual)
                .extracting(
                    Reservation::getId,
                    r -> r.getName().value(),
                    r -> r.getTime().getStartAt(),
                    r -> r.getTheme().getName()
                )
                .containsExactly(tuple(reservation.getId(), "예약자1", LocalTime.of(10, 0), "테마1"));
        }

        @Test
        @Order(3)
        void 삭제된_예약도_함께_조회한다() {
            // given
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            Reservation active = reservationRepository.save(
                Reservation.create(new ReserverName("예약자1"), date, time1, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation deleted = reservationRepository.save(
                Reservation.create(new ReserverName("예약자2"), date, time2, theme, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.update(deleted.delete());

            // when
            List<Reservation> actual = reservationRepository.findAllReservations();

            // then
            assertThat(actual)
                .extracting(Reservation::getId, Reservation::getStatus)
                .containsExactlyInAnyOrder(
                    tuple(active.getId(), ReservationStatus.ACTIVE),
                    tuple(deleted.getId(), ReservationStatus.DELETED)
                );
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
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date1 = LocalDate.now().plusYears(1);
            Reservation reservation1 = reservationRepository.save(
                Reservation.create(new ReserverName("브라운"), date1, time1, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation deletedReservation = reservationRepository.save(
                Reservation.create(new ReserverName("브라운"), date1.plusDays(1), time2, theme, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.save(
                Reservation.create(new ReserverName("제이슨"), date1.plusDays(2), time3, theme, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.update(deletedReservation.delete());

            // when
            List<Reservation> actual = reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("브라운"));

            // then
            assertThat(actual)
                .extracting(Reservation::getId, r -> r.getName().value(), Reservation::getDate)
                .containsExactly(tuple(reservation1.getId(), "브라운", date1));
        }

        @Test
        void 이름이_일치하는_예약이_없으면_빈_목록을_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            reservationRepository.save(
                Reservation.create(new ReserverName("브라운"), LocalDate.now().plusYears(1), time, theme, ReservationStatus.ACTIVE, 1_000L));

            // when
            List<Reservation> actual = reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("제이슨"));

            // then
            assertThat(actual).isEmpty();
        }

        @Test
        void 삭제된_시간과_테마에_연결된_예약의_삭제_상태도_조회한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            reservationRepository.save(
                Reservation.create(new ReserverName("브라운"), LocalDate.now().plusYears(1), time, theme, ReservationStatus.ACTIVE, 1_000L));
            timeRepository.deleteTimeById(time.getId());
            themeRepository.deleteThemeById(theme.getId());

            // when
            List<Reservation> actual = reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("브라운"));

            // then
            assertThat(actual).hasSize(1);
            assertThat(actual.getFirst().getTime().getStatus()).isEqualTo(EntityStatus.DELETED);
            assertThat(actual.getFirst().getTheme().getStatus()).isEqualTo(EntityStatus.DELETED);
        }

        @Test
        void 날짜순_시간순으로_조회한다() {
            // given
            LocalDate date = LocalDate.now().plusYears(1);
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation reservation1 = reservationRepository.save(
                Reservation.create(new ReserverName("브라운"), date, time2, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation reservation2 = reservationRepository.save(
                Reservation.create(new ReserverName("브라운"), date, time1, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation reservation3 = reservationRepository.save(
                Reservation.create(new ReserverName("브라운"), date.minusDays(1), time3, theme, ReservationStatus.ACTIVE, 1_000L));

            // when
            List<Reservation> actual = reservationRepository.findReservationsByNameAndNotDeleted(new ReserverName("브라운"));

            // then
            assertThat(actual)
                .extracting(Reservation::getId)
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
            LocalDate date = LocalDate.now().plusYears(1);
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(12, 0)));
            Time deletedTime = timeRepository.save(Time.create(LocalTime.of(13, 0)));
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "https://example.com/image2.png"));
            reservationRepository.save(Reservation.create(new ReserverName("예약자1"), date, time1, theme1, ReservationStatus.ACTIVE, 1_000L));
            Reservation deletedReservation = reservationRepository.save(
                Reservation.create(new ReserverName("삭제된예약자"), date, time2, theme1, ReservationStatus.ACTIVE, 1_000L));
            Reservation canceledReservation = reservationRepository.save(
                Reservation.create(new ReserverName("취소된예약자"), date, time3, theme1, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.save(Reservation.create(new ReserverName("예약자2"), date, time2, theme2, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.save(Reservation.create(new ReserverName("예약자3"), date.plusDays(1), time2, theme1, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.save(
                Reservation.create(new ReserverName("삭제된시간예약자"), date, deletedTime, theme1, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.update(deletedReservation.delete());
            reservationRepository.update(canceledReservation.cancelActive(new ReserverName("취소된예약자")));
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
        void 대기가_하나일_때_1을_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation waiting = reservationRepository.save(
                Reservation.create(new ReserverName("예약자"), LocalDate.now().plusYears(1), time, theme, ReservationStatus.WAITING, 1_000L));

            // when
            int actual = reservationRepository.countByIdLessThanEqualAndSlot(waiting.getId(), waiting.getSlot().toSlotKey());

            // then
            assertThat(actual).isEqualTo(1);
        }

        @Test
        void 자신보다_이전에_등록된_대기를_포함하여_카운트한다() {
            // given
            LocalDate date = LocalDate.now().plusYears(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            reservationRepository.save(Reservation.create(new ReserverName("예약자1"), date, time, theme, ReservationStatus.WAITING, 1_000L));
            reservationRepository.save(Reservation.create(new ReserverName("예약자2"), date, time, theme, ReservationStatus.WAITING, 1_000L));
            Reservation last = reservationRepository.save(
                Reservation.create(new ReserverName("예약자3"), date, time, theme, ReservationStatus.WAITING, 1_000L));

            // when
            int actual = reservationRepository.countByIdLessThanEqualAndSlot(last.getId(), last.getSlot().toSlotKey());

            // then
            assertThat(actual).isEqualTo(3);
        }

        @Test
        void WAITING이_아닌_상태의_예약은_카운트하지_않는다() {
            // given
            LocalDate date = LocalDate.now().plusYears(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            reservationRepository.save(Reservation.create(new ReserverName("예약자1"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation canceledWaiting = reservationRepository.save(
                Reservation.create(new ReserverName("예약자2"), date, time, theme, ReservationStatus.WAITING, 1_000L));
            reservationRepository.update(canceledWaiting.cancelWaiting(new ReserverName("예약자2")));
            Reservation waiting = reservationRepository.save(
                Reservation.create(new ReserverName("예약자3"), date, time, theme, ReservationStatus.WAITING, 1_000L));

            // when
            int actual = reservationRepository.countByIdLessThanEqualAndSlot(waiting.getId(), waiting.getSlot().toSlotKey());

            // then
            assertThat(actual).isEqualTo(1);
        }
    }

    @Nested
    class 슬롯의_활성_예약_존재_여부_확인 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 슬롯에_활성_예약이_있으면_true를_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation reservation = reservationRepository.save(ReservationFixture.FUTURE.createInstance(time, theme));

            // when
            boolean actual = reservationRepository.existsActiveReservation(reservation.getSlot().toSlotKey());

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 대기만_있고_활성_예약이_없으면_false를_반환한다() {
            // given
            LocalDate date = LocalDate.now().plusYears(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation waiting = reservationRepository.save(
                Reservation.create(new ReserverName("예약자"), date, time, theme, ReservationStatus.WAITING, 1_000L));

            // when
            boolean actual = reservationRepository.existsActiveReservation(waiting.getSlot().toSlotKey());

            // then
            assertThat(actual).isFalse();
        }

        @Test
        void 취소되거나_삭제된_예약이면_false를_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation reservation = reservationRepository.save(ReservationFixture.FUTURE.createInstance(time, theme));
            reservationRepository.update(reservation.cancelActive(new ReserverName(ReservationFixture.FUTURE.getName())));

            // when
            boolean actual = reservationRepository.existsActiveReservation(reservation.getSlot().toSlotKey());

            // then
            assertThat(actual).isFalse();
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
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Theme updateTheme = themeRepository.save(Theme.create("테마2", "설명2", "https://example.com/image2.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            LocalDate updateDate = date.plusDays(1);
            Reservation reservation = reservationRepository.save(
                Reservation.create(new ReserverName("예약자1"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation updateReservation = Reservation.reconstruct(reservation.getId(), new ReserverName("예약자2"),
                updateDate, updateTime, updateTheme, ReservationStatus.ACTIVE);

            // when
            Reservation actual = reservationRepository.update(updateReservation);

            // then
            assertThat(actual.getId()).isEqualTo(reservation.getId());
            assertThat(reservationRepository.findReservationByIdAndNotDeleted(reservation.getId()))
                .get()
                .extracting(
                    r -> r.getName().value(),
                    Reservation::getDate,
                    r -> r.getTime().getId(),
                    r -> r.getTheme().getId()
                )
                .containsExactly("예약자2", updateDate, updateTime.getId(), updateTheme.getId());
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
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation reservation = reservationRepository.save(ReservationFixture.FUTURE.createInstance(time, theme));

            // when
            boolean actual = reservationRepository.existsReservationAndStatus(reservation, reservation.getStatus());

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 상태가_다르면_false를_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation activeReservation = reservationRepository.save(ReservationFixture.FUTURE.createInstance(time, theme));

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
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            Reservation reservation1 = reservationRepository.save(
                Reservation.create(new ReserverName("예약자1"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation reservation2 = reservationRepository.save(
                Reservation.create(new ReserverName("예약자2"), date.plusDays(1), time, theme, ReservationStatus.ACTIVE, 1_000L));

            // when
            reservationRepository.update(reservation1.delete());

            // then
            assertThat(reservationRepository.findAllReservations())
                .extracting(Reservation::getId, Reservation::getStatus)
                .containsExactlyInAnyOrder(
                    tuple(reservation1.getId(), ReservationStatus.DELETED),
                    tuple(reservation2.getId(), ReservationStatus.ACTIVE)
                );
            assertThat(reservationRepository.existsReservationByIdAndNotDeleted(reservation1.getId())).isFalse();
        }
    }

    @Nested
    class 상태_조건부_변경 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 현재_상태와_버전이_기대값과_일치하면_상태를_변경한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));

            Reservation active = reservationRepository.save(ReservationFixture.FUTURE.createInstance(time, theme));

            // when
            reservationRepository.changeStatus(
                active.getId(), active.getVersion(), ReservationStatus.ACTIVE, ReservationStatus.CANCELED);

            // then
            assertThat(reservationRepository.findAllReservations())
                .filteredOn(found -> found.getId().equals(active.getId()))
                .extracting(Reservation::getStatus)
                .containsExactly(ReservationStatus.CANCELED);
        }

        @Test
        void 현재_상태가_기대값과_다르면_낙관적_락_예외가_발생하고_변경하지_않는다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));

            Reservation active = reservationRepository.save(ReservationFixture.FUTURE.createInstance(time, theme));

            // when & then: 기대 상태를 WAITING으로 주지만 실제는 ACTIVE
            assertThatThrownBy(() -> reservationRepository.changeStatus(
                active.getId(), active.getVersion(), ReservationStatus.WAITING, ReservationStatus.ACTIVE))
                .isInstanceOf(OptimisticLockingFailureException.class)
                .hasMessageContaining("예약 상태가 다른 요청에 의해 먼저 변경되었습니다.");

            assertThat(reservationRepository.findAllReservations())
                .filteredOn(found -> found.getId().equals(active.getId()))
                .extracting(Reservation::getStatus)
                .containsExactly(ReservationStatus.ACTIVE);
        }

        @Test
        void 상태가_일치해도_버전이_다르면_낙관적_락_예외가_발생하고_변경하지_않는다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));

            Reservation active = reservationRepository.save(ReservationFixture.FUTURE.createInstance(time, theme));

            // when & then: 상태(ACTIVE)는 일치하지만 오래된 버전(현재 버전 + 1)으로 시도
            assertThatThrownBy(() -> reservationRepository.changeStatus(
                active.getId(), active.getVersion() + 1, ReservationStatus.ACTIVE, ReservationStatus.CANCELED))
                .isInstanceOf(OptimisticLockingFailureException.class)
                .hasMessageContaining("예약 상태가 다른 요청에 의해 먼저 변경되었습니다.");

            assertThat(reservationRepository.findAllReservations())
                .filteredOn(found -> found.getId().equals(active.getId()))
                .extracting(Reservation::getStatus)
                .containsExactly(ReservationStatus.ACTIVE);
        }
    }

    @Nested
    class 낙관적_락 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 버전이_일치하면_수정에_성공한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            Reservation saved = reservationRepository.save(
                Reservation.create(new ReserverName("예약자"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation reread = reservationRepository.findReservationByIdAndNotDeleted(saved.getId()).orElseThrow();

            // when
            Reservation updated = reservationRepository.update(
                reread.update(new ReserverName("예약자"), date.plusDays(1), time, theme));

            // then
            assertThat(updated.getId()).isEqualTo(saved.getId());
            assertThat(reservationRepository.findReservationByIdAndNotDeleted(saved.getId()))
                .get()
                .extracting(Reservation::getDate)
                .isEqualTo(date.plusDays(1));
        }

        @Test
        void 읽은_뒤_다른_요청이_먼저_변경했으면_낙관적_락_예외가_발생한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            Reservation saved = reservationRepository.save(
                Reservation.create(new ReserverName("예약자"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            // 동시 수정 시뮬레이션: 같은 행을 먼저 변경해 버전을 올린다
            reservationRepository.changeStatus(saved.getId(), saved.getVersion(), ReservationStatus.ACTIVE, ReservationStatus.CANCELED);

            // when & then: 오래된 버전(0)을 가진 객체로 수정 시도
            Reservation stale = saved.update(new ReserverName("예약자"), date.plusDays(1), time, theme);
            assertThatThrownBy(() -> reservationRepository.update(stale))
                .isInstanceOf(OptimisticLockingFailureException.class);
        }
    }

    @Nested
    class 죽은_슬롯_조회 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 활성_예약_없이_대기만_있는_슬롯을_조회한다() {
            // given
            LocalDate date = LocalDate.now().plusYears(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            reservationRepository.save(
                Reservation.create(new ReserverName("대기자"), date, time, theme, ReservationStatus.WAITING, 1_000L));

            // when
            List<SlotKey> actual = reservationRepository.findDeadSlotKeys();

            // then
            assertThat(actual)
                .extracting(SlotKey::timeId, SlotKey::themeId, SlotKey::date)
                .containsExactly(tuple(time.getId(), theme.getId(), date));
        }

        @Test
        void 활성_예약이_있는_슬롯은_죽은_슬롯이_아니다() {
            // given
            LocalDate date = LocalDate.now().plusYears(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            reservationRepository.save(
                Reservation.create(new ReserverName("예약자"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.save(
                Reservation.create(new ReserverName("대기자"), date, time, theme, ReservationStatus.WAITING, 1_000L));

            // when
            List<SlotKey> actual = reservationRepository.findDeadSlotKeys();

            // then
            assertThat(actual).isEmpty();
        }

        @Test
        void 같은_슬롯에_대기가_여러개여도_중복없이_하나만_조회한다() {
            // given
            LocalDate date = LocalDate.now().plusYears(1);
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            reservationRepository.save(
                Reservation.create(new ReserverName("대기자1"), date, time, theme, ReservationStatus.WAITING, 1_000L));
            reservationRepository.save(
                Reservation.create(new ReserverName("대기자2"), date, time, theme, ReservationStatus.WAITING, 1_000L));

            // when
            List<SlotKey> actual = reservationRepository.findDeadSlotKeys();

            // then
            assertThat(actual)
                .extracting(SlotKey::timeId, SlotKey::themeId, SlotKey::date)
                .containsExactly(tuple(time.getId(), theme.getId(), date));
        }
    }

    @Nested
    class 활성_또는_대기_예약_존재_여부 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 활성_예약이_있으면_true를_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            Reservation active = reservationRepository.save(ReservationFixture.FUTURE.createInstance(time, theme));

            // when & then
            assertThat(reservationRepository.existsActiveOrWaitingReservation(active.getSlot().toSlotKey())).isTrue();
        }

        @Test
        void 대기_예약이_있으면_true를_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            reservationRepository.save(
                Reservation.create(new ReserverName("예약자1"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            Reservation waiting = reservationRepository.save(
                Reservation.create(new ReserverName("예약자2"), date, time, theme, ReservationStatus.WAITING, 1_000L));

            // when & then
            assertThat(reservationRepository.existsActiveOrWaitingReservation(waiting.getSlot().toSlotKey())).isTrue();
        }

        @Test
        void 취소되거나_삭제된_예약만_있으면_false를_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "https://example.com/image1.png"));
            LocalDate date = LocalDate.now().plusYears(1);
            Reservation active = reservationRepository.save(
                Reservation.create(new ReserverName("예약자"), date, time, theme, ReservationStatus.ACTIVE, 1_000L));
            reservationRepository.update(active.delete());

            // when & then
            assertThat(reservationRepository.existsActiveOrWaitingReservation(active.getSlot().toSlotKey())).isFalse();
        }
    }
}
