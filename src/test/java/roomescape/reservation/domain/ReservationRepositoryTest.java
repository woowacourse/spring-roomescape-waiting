package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.fixture.ReservationFixture;
import roomescape.time.domain.fixture.ReservationTimeFixture;
import roomescape.theme.domain.fixture.ThemeFixture;
import roomescape.support.datasource.ReservationDataSource;
import roomescape.support.datasource.BaseRepositoryTest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.dto.ReservationQueryResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationRepositoryTest extends BaseRepositoryTest {

    private final ReservationTime reservationTime = ReservationTimeFixture.createDefaultReservationTime();
    private final Theme theme = ThemeFixture.createThemeWithId();

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
        dataSource.insertTheme(theme.getName(), theme.getThumbnailImageUrl(), theme.getDescription());
        dataSource.insertReservationTime(reservationTime.getStartAt());
    }

    @Test
    void 예약을_저장하고_ID로_조회한다() {
        // given
        Reservation reservation = ReservationFixture.createDefaultReservation("바니", theme, reservationTime);

        // when
        Reservation saved = reservationRepository.save(reservation);

        // then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(dataSource.hasReservationById(saved.getId())).isTrue();
    }

    @Test
    void 특정_테마의_특정_날짜와_시간에_승인된_예약이_존재하는지_확인한다() {
        // given
        LocalDate date = LocalDate.now(ReservationFixture.FIXED_CLOCK).plusDays(1);
        reservationRepository.save(Reservation.create("바니", date, reservationTime, theme, Status.RESERVED,
                ReservationFixture.FIXED_CLOCK));

        // when & then
        assertThat(reservationRepository.existsActiveReservationByDateTimeAndTheme(reservationTime.getId(),
                theme.getId(), date)).isTrue();
        assertThat(reservationRepository.existsActiveReservationByDateTimeAndTheme(99L, theme.getId(), date)).isFalse();
    }

    @Test
    void 예약_정보를_수정한다() {
        // given
        Reservation reservation = reservationRepository.save(
                ReservationFixture.createDefaultReservation("바니", theme, reservationTime));

        // when
        reservationRepository.update(reservation.cancel());

        // then
        Optional<Reservation> found = reservationRepository.findById(reservation.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void 페이징_조건에_맞는_승인_예약_목록을_조회한다() {
        // given
        dataSource.insertReservation("바니", LocalDate.now().plusDays(1), 1L, 1L, "RESERVED");
        dataSource.insertReservation("포비", LocalDate.now().plusDays(2), 1L, 1L, "WAITING");

        // when
        List<Reservation> reservations = reservationRepository.findAll(0, 10);

        // then
        assertThat(reservations).hasSize(1).extracting(Reservation::getStatus).containsExactly(Status.RESERVED);
    }

    @Test
    void 다음_대기_예약을_조회한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        dataSource.insertReservation("바니", date, 1L, 1L, "RESERVED");
        dataSource.insertReservation("포비", date, 1L, 1L, "WAITING");

        // when
        Optional<Reservation> reservation = reservationRepository.findNextPendingReservation(date, 1L, 1L);

        // then
        assertThat(reservation).isPresent().get().extracting(Reservation::getName).isEqualTo("포비");
    }

    @Test
    void 특정_사용자_이름으로_예약과_대기순서를_조회한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        dataSource.insertReservation("바니", date, 1L, 1L, "RESERVED");
        dataSource.insertReservation("포비", date, 1L, 1L, "WAITING");

        // when
        List<ReservationQueryResult> results = reservationRepository.findAllByName("포비");

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().pendingIndex()).isEqualTo(1L);
    }

    @Test
    void 특정_테마와_날짜로_승인_예약을_조회한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        dataSource.insertReservationTime(LocalTime.of(11, 0));
        dataSource.insertReservation("바니", date, 1L, 1L, "RESERVED");
        dataSource.insertReservation("포비", date, 1L, 2L, "WAITING");

        // when
        List<Reservation> reservations = reservationRepository.findByThemeAndDate(1L, date);

        // then
        assertThat(reservations).hasSize(1).extracting(Reservation::getName).containsExactly("바니");
    }
}
