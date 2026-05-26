package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.reservation.fixture.ReservationFixture.reservation;

import java.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.date.repository.JdbcReservationDateRepository;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.theme.repository.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

@JdbcTest
class ReservationTimeRepositoryTest {

    private JdbcReservationTimeRepository jdbcReservationTimeRepository;
    private JdbcReservationDateRepository jdbcReservationDateRepository;
    private JdbcThemeRepository jdbcThemeRepository;
    private JdbcReservationRepository jdbcReservationRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcReservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        jdbcReservationDateRepository = new JdbcReservationDateRepository(jdbcTemplate);
        jdbcThemeRepository = new JdbcThemeRepository(jdbcTemplate);
        jdbcReservationRepository = new JdbcReservationRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void saveTime() {
        // given
        List<ReservationTime> emptyTimes = List.of();

        // when
        jdbcReservationTimeRepository.save(ReservationTimeFixture.time15());

        // then
        assertThat(jdbcReservationTimeRepository.findAll())
                .hasSize(emptyTimes.size() + 1);
    }

    @Test
    @DisplayName("모든 예약 시간 정보를 조회한다.")
    void findAll() {
        // given
        List<ReservationTime> reservationTimes = List.of(
                ReservationTimeFixture.time15(),
                ReservationTimeFixture.time16(),
                ReservationTimeFixture.time17()
        );
        List<ReservationTime> savedTimes = saveAll(reservationTimes);

        // when
        List<ReservationTime> actual = jdbcReservationTimeRepository.findAll();

        // then
        assertThat(actual)
                .hasSize(savedTimes.size());
    }

    @Test
    @DisplayName("등록된 예약 시간을 활성화 상태로 변경한다.")
    void updateStatus_active() {
        // given
        ReservationTime saved = saveTime(ReservationTimeFixture.time15());
        saved.updateStatus(true);
        updateStatus(saved);

        // when
        ReservationTime actual = jdbcReservationTimeRepository.findById(saved.getId()).get();

        // then
        Assertions.assertThat(actual.isActive())
                .isTrue();
    }

    @Test
    @DisplayName("등록된 예약 시간을 비활성화 상태로 변경한다.")
    void updateStatus_inactive() {
        // given
        ReservationTime saved = saveTime(ReservationTimeFixture.activeTime15());
        saved.updateStatus(false);
        updateStatus(saved);

        // when
        ReservationTime actual = jdbcReservationTimeRepository.findById(saved.getId()).get();

        // then
        Assertions.assertThat(actual.isActive())
                .isFalse();
    }

    @Test
    @DisplayName("예약 시작 시간 값으로 예약 시간이 존재하는지 확인한다.")
    void existsByStartAt() {
        // given
        LocalTime duplicatedTime = LocalTime.of(15, 0);
        LocalTime nonSavedTime = LocalTime.of(12, 0);
        saveTime(ReservationTime.create(duplicatedTime));

        // when & then
        assertThat(jdbcReservationTimeRepository.existsByStartAt(duplicatedTime)).isTrue();
        assertThat(jdbcReservationTimeRepository.existsByStartAt(nonSavedTime)).isFalse();
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회한다.")
    void findAvailableTimes() {
        // given
        ReservationTime reservedTime15 = saveTime(ReservationTimeFixture.activeTime15());
        ReservationTime reservedTime16 = saveTime(ReservationTimeFixture.activeTime16());
        ReservationTime nonReservedTime = saveTime(ReservationTimeFixture.activeTime17());

        ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
        Theme theme = saveTheme(ThemeFixture.activeTheme());

        saveReservation(date, reservedTime15, theme);
        saveReservation(date, reservedTime16, theme);

        // when
        List<ReservationTime> availableTimes = jdbcReservationTimeRepository.findAvailableByDateIdAndThemeId(
                date.getId(),
                theme.getId()
        );

        // then
// then
        assertThat(availableTimes)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .containsExactlyInAnyOrder(
                        reservedTime15,
                        reservedTime16,
                        nonReservedTime
                );
    }

    private List<ReservationTime> saveAll(List<ReservationTime> reservationTimes) {
        List<ReservationTime> savedTimes = new ArrayList<>();
        for (ReservationTime reservationTime : reservationTimes) {
            savedTimes.add(saveTime(reservationTime));
        }
        return savedTimes;
    }

    private ReservationTime saveTime(ReservationTime reservationTime) {
        return jdbcReservationTimeRepository.save(reservationTime);
    }

    private ReservationDate saveDate(ReservationDate reservationDate) {
        return jdbcReservationDateRepository.save(reservationDate);
    }

    private Theme saveTheme(Theme theme) {
        return jdbcThemeRepository.save(theme);
    }

    private void saveReservation(ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        jdbcReservationRepository.save(reservation("송송", reservationDate, reservationTime, theme));
    }

    private boolean updateStatus(ReservationTime saved) {
        return jdbcReservationTimeRepository.updateStatus(saved);
    }

}
