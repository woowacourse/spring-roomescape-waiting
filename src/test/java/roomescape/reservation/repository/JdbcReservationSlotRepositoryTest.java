package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.test_config.fixture.SqlFixtureGenerator;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({JdbcReservationSlotRepository.class, SqlFixtureGenerator.class})
class JdbcReservationSlotRepositoryTest {

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private SqlFixtureGenerator sqlFixtureGenerator;

    @Test
    @DisplayName("예약 슬롯이 없으면 생성하고 반환한다.")
    void upsert_create() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationSlot reservationSlot = ReservationSlot.create(LocalDate.of(2023, 8, 5), time, theme);

        // when
        ReservationSlot saved = reservationSlotRepository.upsert(reservationSlot);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved)
                .extracting(ReservationSlot::getDate, ReservationSlot::getTime, ReservationSlot::getTheme)
                .containsExactly(reservationSlot.getDate(), time, theme);
    }

    @Test
    @DisplayName("예약 슬롯이 이미 있으면 기존 슬롯을 반환한다.")
    void upsert_findExisting() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationSlot saved = sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme);

        // when
        ReservationSlot found = reservationSlotRepository.upsert(
                ReservationSlot.create(saved.getDate(), saved.getTime(), saved.getTheme()));

        // then
        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("id로 예약 슬롯을 락과 함께 조회한다.")
    void findByIdWithLock() {
        // given
        ReservationTime time = sqlFixtureGenerator.insertReservationTime(LocalTime.of(10, 0));
        Theme theme = sqlFixtureGenerator.insertTheme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
        ReservationSlot reservationSlot = sqlFixtureGenerator.insertReservationSlot(LocalDate.of(2023, 8, 5), time, theme);

        // when
        Optional<ReservationSlot> optionalSlot = reservationSlotRepository.findByIdWithLock(reservationSlot.getId());

        // then
        assertThat(optionalSlot).isPresent();
        assertThat(optionalSlot.get())
                .extracting(ReservationSlot::getId, ReservationSlot::getDate, ReservationSlot::getTime, ReservationSlot::getTheme)
                .containsExactly(reservationSlot.getId(), reservationSlot.getDate(), time, theme);
    }

    @Test
    @DisplayName("존재하지 않는 예약 슬롯은 락과 함께 조회되지 않는다.")
    void findByIdWithLock_notFound() {
        // when
        Optional<ReservationSlot> optionalSlot = reservationSlotRepository.findByIdWithLock(1L);

        // then
        assertThat(optionalSlot).isEmpty();
    }
}
