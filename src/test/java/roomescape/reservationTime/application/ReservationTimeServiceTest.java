package roomescape.reservationTime.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservation.application.ReservationTimeReferenceAdapter;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.reservationTime.fake.FakeReservationTimeRepository;
import roomescape.theme.domain.Theme;

class ReservationTimeServiceTest {

    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;
    private ReservationTimeService reservationTimeService;
    private ReservationTimeReference reservationReference;


    @BeforeEach
    void setUp() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationRepository = new FakeReservationRepository();
        reservationReference = new ReservationTimeReferenceAdapter(reservationRepository);
        reservationTimeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationReference
        );
    }

    @Test
    @DisplayName("예약 시간을 저장한다")
    void saveTime_success() {
        // given
        LocalTime testStartAt = LocalTime.now();

        // when
        ReservationTime reservationTime = saveTime(testStartAt);

        // then
        assertThat(reservationTime.getId()).isNotNull();
        assertThat(reservationTime.getStartAt()).isEqualTo(testStartAt);
        assertThat(reservationTimeRepository.findById(reservationTime.getId())).contains(reservationTime);
    }

    @Test
    @DisplayName("예약 시간 목록을 조회한다")
    void getTimes_success() {
        // given
        LocalTime testStartAt = LocalTime.now();
        saveTime(testStartAt);

        // when
        List<ReservationTime> reservationTimes = reservationTimeService.getTimes();

        // then
        assertThat(reservationTimes)
                .extracting(ReservationTime::getStartAt)
                .containsExactly(testStartAt);
    }

    @Test
    @DisplayName("예약 시간이 없으면 빈 목록을 반환한다")
    void getTimes_success_when_empty() {
        // when
        List<ReservationTime> reservationTimes = reservationTimeService.getTimes();

        // then
        assertThat(reservationTimes).isEmpty();
    }

    @Test
    @DisplayName("날짜와 테마 기준으로 예약된 시간을 조회한다")
    void getBookedTimes_success() {
        // given
        ReservationTime bookedTime = saveTime(LocalTime.of(10, 0));
        saveTime(LocalTime.of(11, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com/thumb-nail/1");
        LocalDate date = LocalDate.now().plusDays(1);
        reservationRepository.save(Reservation.create("테스터", date, bookedTime, theme));

        // when
        Set<Long> result = reservationTimeService.getBookedTimes(date, theme.getId());

        // then
        assertThat(result)
                .containsExactlyInAnyOrder(bookedTime.getId());
    }

    @Test
    @DisplayName("예약 시간을 삭제한다")
    void deleteTime_success() {
        // given
        LocalTime testStartAt = LocalTime.now();
        ReservationTime saved = saveTime(testStartAt);

        // when
        reservationTimeService.deleteTime(saved.getId());

        // then
        assertThat(reservationTimeRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 예외가 발생한다")
    void deleteReservationTime_fail_with_not_found_time() {
        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("예약 시간을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("예약에서 참조 중인 예약 시간을 삭제하면 예외가 발생한다")
    void deleteTime_fail_with_referenced_time() {
        // given
        LocalTime testStartAt = LocalTime.now().plusHours(1);
        ReservationTime savedReservationTime = saveTime(testStartAt);
        reservationRepository.save(Reservation.create("테스터", LocalDate.now(), savedReservationTime, null));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(savedReservationTime.getId()))
                .isInstanceOf(BusinessException.class);
        assertThat(reservationTimeRepository.findById(savedReservationTime.getId())).contains(savedReservationTime);
    }

    private ReservationTime saveTime(LocalTime startAt) {
        return reservationTimeService.saveTime(new ReservationTimeCreateCommand(startAt));
    }
}
