package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.infrastructure.ReservationTimeJdbcTemplateRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeJdbcTemplateRepository;

@JdbcTest
@Import({
        ReservationJdbcTemplateRepository.class,
        ReservationTimeJdbcTemplateRepository.class,
        ThemeJdbcTemplateRepository.class
})
class ReservationJdbcTemplateRepositoryTest {

    private static final String TEST_NAME = "홍길동";
    private static final LocalDate DATE_TODAY = LocalDate.now().plusDays(1);
    private static final LocalDate DATE_TOMORROW = LocalDate.now().plusDays(2);

    private final ReservationJdbcTemplateRepository reservationRepository;
    private final ReservationTimeJdbcTemplateRepository timeRepository;
    private final ThemeJdbcTemplateRepository themeRepository;

    @Autowired
    ReservationJdbcTemplateRepositoryTest(ReservationJdbcTemplateRepository reservationRepository,
                                          ReservationTimeJdbcTemplateRepository timeRepository,
                                          ThemeJdbcTemplateRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    private ReservationTime savedTime1;
    private ReservationTime savedTime2;
    private Theme savedTheme1;
    private Theme savedTheme2;

    @BeforeEach
    void setUp() {
        savedTime1 = timeRepository.save(ReservationTime.create(LocalTime.of(1, 0)));
        savedTime2 = timeRepository.save(ReservationTime.create(LocalTime.of(2, 0)));
        savedTheme1 = themeRepository.save(
                Theme.create("테스트 테마1", "테스트 테마 설명1", "https://good.com/thumb-nail/1"));
        savedTheme2 = themeRepository.save(
                Theme.create("테스트 테마2", "테스트 테마 설명2", "https://good.com/thumb-nail/2"));
    }

    @Test
    @DisplayName("예약을 저장하면 생성된 ID를 포함한 예약 객체를 반환한다")
    void save_success() {
        // given
        Reservation reservation = Reservation.create(TEST_NAME, DATE_TODAY, savedTime1, savedTheme1);

        // when
        Reservation savedReservation = reservationRepository.save(reservation);

        // then
        Assertions.assertNotNull(savedReservation.getId());
        Assertions.assertEquals(TEST_NAME, savedReservation.getName());
    }

    @Test
    @DisplayName("전체 예약 목록을 조회한다")
    void findByDateAndThemeId_findAll_when_both_null() {
        // given
        reservationRepository.save(Reservation.create(TEST_NAME + "1", DATE_TODAY, savedTime1, savedTheme1));
        reservationRepository.save(Reservation.create(TEST_NAME + "2", DATE_TOMORROW, savedTime2, savedTheme2));

        // when
        List<Reservation> result = reservationRepository.findAll();

        // then
        Assertions.assertEquals(2, result.size());
    }

    @Test
    @DisplayName("date만 있으면 해당 날짜의 예약만 조회한다")
    void findByDateAndThemeId_filter_by_date_only() {
        // given
        reservationRepository.save(Reservation.create(TEST_NAME + "1", DATE_TODAY, savedTime1, savedTheme1));
        reservationRepository.save(Reservation.create(TEST_NAME + "2", DATE_TODAY, savedTime2, savedTheme2));
        reservationRepository.save(Reservation.create(TEST_NAME + "3", DATE_TOMORROW, savedTime1, savedTheme1));

        // when
        List<Reservation> result = reservationRepository.findByDateAndThemeId(DATE_TODAY, null);

        // then
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().allMatch(r -> r.getDate().equals(DATE_TODAY)));
    }

    @Test
    @DisplayName("themeId만 있으면 해당 테마의 예약만 조회한다")
    void findByDateAndThemeId_filter_by_theme_only() {
        // given
        reservationRepository.save(Reservation.create(TEST_NAME + "1", DATE_TODAY, savedTime1, savedTheme1));
        reservationRepository.save(Reservation.create(TEST_NAME + "2", DATE_TOMORROW, savedTime2, savedTheme1));
        reservationRepository.save(Reservation.create(TEST_NAME + "3", DATE_TODAY, savedTime1, savedTheme2));

        // when
        List<Reservation> result = reservationRepository.findByDateAndThemeId(null, savedTheme1.getId());

        // then
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().allMatch(r -> r.getTheme().getId().equals(savedTheme1.getId())));
    }

    @Test
    @DisplayName("date, themeId 모두 있으면 두 조건 모두 일치하는 예약만 조회한다")
    void findByDateAndThemeId_filter_by_date_and_theme() {
        // given
        reservationRepository.save(Reservation.create(TEST_NAME + "1", DATE_TODAY, savedTime1, savedTheme1));
        reservationRepository.save(Reservation.create(TEST_NAME + "2", DATE_TODAY, savedTime2, savedTheme2));
        reservationRepository.save(Reservation.create(TEST_NAME + "3", DATE_TOMORROW, savedTime1, savedTheme1));

        // when
        List<Reservation> result = reservationRepository.findByDateAndThemeId(DATE_TODAY, savedTheme1.getId());

        // then
        Assertions.assertEquals(1, result.size());
        Reservation only = result.get(0);
        Assertions.assertEquals(DATE_TODAY, only.getDate());
        Assertions.assertEquals(savedTheme1.getId(), only.getTheme().getId());
    }

    @Test
    @DisplayName("일치하는 예약이 없으면 빈 목록을 반환한다")
    void findByDateAndThemeId_returns_empty_when_no_match() {
        // given
        reservationRepository.save(Reservation.create(TEST_NAME, DATE_TODAY, savedTime1, savedTheme1));

        // when
        List<Reservation> result = reservationRepository.findByDateAndThemeId(DATE_TOMORROW, savedTheme2.getId());

        // then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("특정 시간 ID를 참조하는 예약이 존재하는지 확인한다")
    void existsByReservationTimeId_success() {
        // given
        reservationRepository.save(Reservation.create(TEST_NAME, DATE_TODAY, savedTime1, savedTheme1));

        // when
        boolean exists = reservationRepository.existsByReservationTimeId(savedTime1.getId());
        boolean notExists = reservationRepository.existsByReservationTimeId(999L);

        // then
        Assertions.assertTrue(exists);
        Assertions.assertFalse(notExists);
    }

    @Test
    @DisplayName("ID를 기반으로 예약을 삭제한다")
    void deleteById_success() {
        // given
        Reservation saved = reservationRepository.save(
                Reservation.create("삭제대상", DATE_TODAY, savedTime1, savedTheme1)
        );

        // when
        Long deleteTargetId = saved.getId();
        reservationRepository.deleteById(deleteTargetId);

        // then
        Optional<Reservation> deleteTargetFindResult = reservationRepository.findAll()
                .stream()
                .filter(reservation -> deleteTargetId.equals(reservation.getId()))
                .findAny();

        Assertions.assertTrue(deleteTargetFindResult.isEmpty());
    }

    @Test
    @DisplayName("date, timeId, themeId가 모두 일치하는 예약을 조회한다")
    void findByDateAndTimeIdAndThemeId_success() {
        // given
        Reservation savedReservation = reservationRepository.save(
                Reservation.create(TEST_NAME, DATE_TODAY, savedTime1, savedTheme1)
        );

        reservationRepository.save(
                Reservation.create(TEST_NAME + "2", DATE_TODAY, savedTime2, savedTheme1)
        );

        // when
        Optional<Reservation> result = reservationRepository.findByDateAndTimeIdAndThemeId(
                DATE_TODAY,
                savedTime1.getId(),
                savedTheme1.getId()
        );

        // then
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(savedReservation.getId(), result.get().getId());
    }

    @Test
    @DisplayName("특정 테마 ID를 참조하는 예약이 존재하는지 확인한다")
    void existsByThemeId_success() {
        // given
        reservationRepository.save(Reservation.create(TEST_NAME, DATE_TODAY, savedTime1, savedTheme1));

        // when
        boolean exists = reservationRepository.existsByThemeId(savedTime1.getId());
        boolean notExists = reservationRepository.existsByThemeId(999L);

        // then
        Assertions.assertTrue(exists);
        Assertions.assertFalse(notExists);
    }

    @Test
    @DisplayName("이름을 기반으로 예약 목록을 조회한다")
    void findByName_success() {
        // given
        Reservation savedReservation1 = reservationRepository.save(
                Reservation.create(TEST_NAME, DATE_TODAY, savedTime1, savedTheme1)
        );
        Reservation savedReservation2 = reservationRepository.save(
                Reservation.create(TEST_NAME, DATE_TOMORROW, savedTime2, savedTheme2)
        );

        // when
        List<Reservation> result = reservationRepository.findByName(TEST_NAME);

        // then
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(r -> r.getId().equals(savedReservation1.getId())));
        Assertions.assertTrue(result.stream().anyMatch(r -> r.getId().equals(savedReservation2.getId())));
    }

    @Test
    @DisplayName("이름과 예약 ID를 기반으로 예약 일정을 수정한다")
    void updateSchedule_success() {
        // given
        Reservation savedReservation = reservationRepository.save(
                Reservation.create(TEST_NAME, DATE_TODAY, savedTime1, savedTheme1)
        );

        // when
        Reservation updateReservation = savedReservation.update(TEST_NAME, DATE_TOMORROW, savedTime2);

        reservationRepository.updateSchedule(updateReservation);

        // then
        Reservation updatedReservation = reservationRepository.findById(savedReservation.getId())
                .orElseThrow();

        assertThat(updatedReservation.getDate()).isEqualTo(DATE_TOMORROW);
        assertThat(updatedReservation.getTime().getId()).isEqualTo(savedTime2.getId());
        assertThat(updatedReservation.getName()).isEqualTo(TEST_NAME);
        assertThat(updatedReservation.getTheme().getId()).isEqualTo(savedTheme1.getId());
    }

    @Test
    @DisplayName("이름과 예약 ID를 기반으로 자신의 예약을 삭제한다")
    void deleteByIdAndName_success() {
        // given
        Reservation savedReservation = reservationRepository.save(
                Reservation.create(TEST_NAME, DATE_TODAY, savedTime1, savedTheme1)
        );

        // when
        reservationRepository.deleteByIdAndName(savedReservation.getId(), TEST_NAME);

        // then
        Optional<Reservation> result = reservationRepository.findById(savedReservation.getId());
        Assertions.assertTrue(result.isEmpty());
    }
}
