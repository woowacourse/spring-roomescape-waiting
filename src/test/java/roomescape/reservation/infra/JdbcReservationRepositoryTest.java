package roomescape.reservation.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.fixture.ReservationFixture;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.User;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import(JdbcReservationRepository.class)
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("ID로 예약 조회를 테스트합니다.")
    @Test
    void find_by_id() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long reservationId = testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                timeId
        );
        User stark = ReservationFixture.userNameStark();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservation.getId()).isEqualTo(reservationId);
            softly.assertThat(reservation.getUser()).isEqualTo(stark);
            softly.assertThat(reservation.getSlot().date()).isEqualTo(date);
            softly.assertThat(reservation.getSlot().themeId()).isEqualTo(themeId);
            softly.assertThat(reservation.getSlot().timeId()).isEqualTo(timeId);
        });
    }

    @DisplayName("존재하지 않는 ID로 예약 조회 시 빈 Optional 반환을 테스트합니다.")
    @Test
    void find_by_id_not_found() {
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

    @DisplayName("방탈출 예약 추가를 테스트합니다.")
    @Test
    void save_user_reservation() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        User stark = ReservationFixture.userNameStark();

        Reservation reservation = Reservation.builder()
                .user(stark)
                .slot(ReservationSlot.builder()
                        .date(LocalDate.of(2026, 5, 4))
                        .themeId(themeId)
                        .timeId(timeId)
                        .startAt(LocalTime.of(9, 0))
                        .build())
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(savedReservation.getUser()).isEqualTo(stark);
            assertSoftly.assertThat(savedReservation.getSlot().date()).isEqualTo(LocalDate.of(2026, 5, 4));
            assertSoftly.assertThat(savedReservation.getSlot().themeId()).isEqualTo(themeId);
            assertSoftly.assertThat(savedReservation.getSlot().timeId()).isEqualTo(timeId);
        });
    }

    @DisplayName("동일한 슬롯으로 예약 추가 시 유니크 제약 위반 예외를 테스트합니다.")
    @Test
    void save_duplicate_slot_exception() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 4);
        testHelper.insertReservation("스타크", date, themeId, timeId);
        User pino = ReservationFixture.userNamePino();

        Reservation reservation = Reservation.builder()
                .user(pino)
                .slot(ReservationSlot.builder()
                        .date(date)
                        .themeId(themeId)
                        .timeId(timeId)
                        .startAt(LocalTime.of(9, 0))
                        .build())
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(UniqueConstraintViolationException.class);
    }

    @DisplayName("존재하지 않는 timeId로 예약 추가 시 DataIntegrityViolationException 발생을 테스트합니다.")
    @Test
    void save_not_exists_time_id_exception() {
        Long notExistTimeId = 999L;
        Long themeId = testHelper.insertTheme("theme name", "theme description", "theme img url");
        LocalDate date = LocalDate.of(2026, 5, 4);
        User stark = ReservationFixture.userNameStark();

        Reservation reservation = Reservation.builder()
                .user(stark)
                .slot(ReservationSlot.builder()
                        .date(date)
                        .themeId(themeId)
                        .timeId(notExistTimeId)
                        .startAt(LocalTime.of(9, 0))
                        .build())
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("존재하지 않는 themeId로 예약 추가 시 DataIntegrityViolationException 발생을 테스트합니다.")
    @Test
    void save_not_exists_theme_id_exception() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long notExistThemeId = 999L;
        LocalDate date = LocalDate.of(2026, 5, 4);
        User stark = ReservationFixture.userNameStark();

        Reservation reservation = Reservation.builder()
                .user(stark)
                .slot(ReservationSlot.builder()
                        .date(date)
                        .themeId(notExistThemeId)
                        .timeId(timeId)
                        .startAt(LocalTime.of(9, 0))
                        .build())
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("방탈출 예약 삭제를 테스트합니다.")
    @Test
    void delete_user_reservation() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long reservationId = testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                timeId
        );

        assertThat(reservationRepository.delete(reservationId)).isEqualTo(1);
    }

    @DisplayName("사용자와 슬롯이 동일한 예약 존재 여부 확인을 테스트합니다.")
    @Test
    void exists_by_user_and_slot() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                nineTimeId
        );

        ReservationSlot sameSlot = ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(nineTimeId)
                .startAt(LocalTime.of(9, 0))
                .build();
        ReservationSlot differentSlot = ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(tenTimeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservationRepository.existsByUserAndSlot("스타크", sameSlot)).isTrue();
            softly.assertThat(reservationRepository.existsByUserAndSlot("카야", sameSlot)).isFalse();
            softly.assertThat(reservationRepository.existsByUserAndSlot("스타크", differentSlot)).isFalse();
        });
    }

    @DisplayName("예약 업데이트를 테스트합니다.")
    @Test
    void update_reservation() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long reservationId = testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                timeId
        );
        User stark = ReservationFixture.userNameStark();

        LocalDate newDate = LocalDate.of(2026, 5, 8);
        Long newTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));

        Reservation updateReservation = Reservation.builder()
                .id(reservationId)
                .user(stark)
                .slot(ReservationSlot.builder()
                        .date(newDate)
                        .themeId(themeId)
                        .timeId(newTimeId)
                        .startAt(LocalTime.of(10, 0))
                        .build())
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        reservationRepository.update(updateReservation.getId(), updateReservation.getSlot());

        Reservation updated = reservationRepository.findById(reservationId).orElseThrow();

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(updated.getSlot().date()).isEqualTo(newDate);
            assertSoftly.assertThat(updated.getSlot().timeId()).isEqualTo(newTimeId);
        });
    }

    @DisplayName("이미 예약이 존재하는 슬롯으로 업데이트 시 유니크 제약 위반 예외를 테스트합니다.")
    @Test
    void update_duplicate_slot_exception() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long nineTimeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long tenTimeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        Long updateReservationId = testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                nineTimeId
        );
        testHelper.insertReservation(
                "피노",
                date,
                themeId,
                tenTimeId
        );

        ReservationSlot duplicateSlot = ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(tenTimeId)
                .startAt(LocalTime.of(10, 0))
                .build();

        assertThatThrownBy(() -> reservationRepository.update(updateReservationId, duplicateSlot))
                .isInstanceOf(UniqueConstraintViolationException.class);
    }
}
