package roomescape.reservation.infra;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.fixture.ReservationFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import(JdbcReservationRepository.class)
class ReservationTableConstraintTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationRepository reservationRepository;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("동일한 PK 삽입 시 DuplicateKeyException(PK 제약 위반) 발생 테스트.")
    @Test
    void save_duplicate_id_exception() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);

        jdbcTemplate.update(
                "INSERT INTO reservation (id, name, date, theme_id, time_id, status) VALUES (?, ?, ?, ?, ?, ?)",
                1L, "스타크", date, themeId, timeId, ReservationStatus.PAYMENT_PENDING.name());

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO reservation (id, name, date, theme_id, time_id, status) VALUES (?, ?, ?, ?, ?, ?)",
                1L, "피노", date, themeId, timeId, ReservationStatus.PAYMENT_PENDING.name()))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @DisplayName("유니크 제약 조건인 동일한 슬롯(date, theme_id, time_id)으로 삽입 시 DuplicateKeyException(Unique 제약 위반) 발생 테스트.")
    @Test
    void save_duplicate_slot_exception() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id, status) VALUES (?, ?, ?, ?, ?)",
                "스타크", date, themeId, timeId, ReservationStatus.PAYMENT_PENDING.name());

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id, status) VALUES (?, ?, ?, ?, ?)",
                "피노", date, themeId, timeId, ReservationStatus.PAYMENT_PENDING.name()))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @DisplayName("NOT NULL 컬럼(name)에 null 삽입 시 DataIntegrityViolationException(Not Null 제약 위반) 발생 테스트.")
    @Test
    void save_null_name_exception() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id, status) VALUES (?, ?, ?, ?, ?)",
                null, date, themeId, timeId, ReservationStatus.PAYMENT_PENDING.name()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("VARCHAR(255) 크기를 초과하는 name 삽입 시 DataIntegrityViolationException(크기 제한 위반) 발생 테스트.")
    @Test
    void save_too_long_name_exception() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        String outOfRangeName = "a".repeat(256);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id, status) VALUES (?, ?, ?, ?, ?)",
                outOfRangeName, date, themeId, timeId, ReservationStatus.PAYMENT_PENDING.name()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("존재하지 않는 theme_id로 예약 저장 시 DataIntegrityViolationException(FK 위반) 발생 테스트.")
    @Test
    void save_not_exist_theme_id_exception() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        Long notExistThemeId = 999L;

        Reservation reservation = Reservation.builder()
                .user(ReservationFixture.userNameStark())
                .slot(ReservationSlot.builder()
                        .date(LocalDate.of(2026, 5, 6))
                        .themeId(notExistThemeId)
                        .timeId(timeId)
                        .startAt(LocalTime.of(9, 0))
                        .build())
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("존재하지 않는 time_id로 예약 저장 시 DataIntegrityViolationException(FK 위반) 발생 테스트.")
    @Test
    void save_not_exist_time_id_exception() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long notExistTimeId = 999L;

        Reservation reservation = Reservation.builder()
                .user(ReservationFixture.userNameStark())
                .slot(ReservationSlot.builder()
                        .date(LocalDate.of(2026, 5, 6))
                        .themeId(themeId)
                        .timeId(notExistTimeId)
                        .startAt(LocalTime.of(9, 0))
                        .build())
                .status(ReservationStatus.PAYMENT_PENDING)
                .build();

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
