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
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import(JdbcReservationRepository.class)
public class ReservationTableConstraintTest {

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
    void test_1() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);

        jdbcTemplate.update(
                "INSERT INTO reservation (id, name, date, theme_id, time_id) VALUES (?, ?, ?, ?, ?)",
                1L, "스타크", date, themeId, timeId);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO reservation (id, name, date, theme_id, time_id) VALUES (?, ?, ?, ?, ?)",
                1L, "피노", date, themeId, timeId))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @DisplayName("유니크 제약 조건인 동일한 슬롯(date, theme_id, time_id)으로 삽입 시 DuplicateKeyException(Unique 제약 위반) 발생 테스트.")
    @Test
    void test_2() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);

        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id) VALUES (?, ?, ?, ?)",
                "스타크", date, themeId, timeId);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id) VALUES (?, ?, ?, ?)",
                "피노", date, themeId, timeId))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @DisplayName("NOT NULL 컬럼(name)에 null 삽입 시 DataIntegrityViolationException(Not Null 제약 위반) 발생 테스트.")
    @Test
    void test_3() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id) VALUES (?, ?, ?, ?)",
                null, date, themeId, timeId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("VARCHAR(255) 크기를 초과하는 name 삽입 시 DataIntegrityViolationException(크기 제한 위반) 발생 테스트.")
    @Test
    void test_4() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));
        LocalDate date = LocalDate.of(2026, 5, 6);
        String outOfRangeName = "a".repeat(256);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id) VALUES (?, ?, ?, ?)",
                outOfRangeName, date, themeId, timeId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("존재하지 않는 theme_id로 예약 저장 시 DataIntegrityViolationException(FK 위반) 발생 테스트.")
    @Test
    void test_5() {
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
                .build();

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("존재하지 않는 time_id로 예약 저장 시 DataIntegrityViolationException(FK 위반) 발생 테스트.")
    @Test
    void test_6() {
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
                .build();

        assertThatThrownBy(() -> reservationRepository.save(reservation))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
