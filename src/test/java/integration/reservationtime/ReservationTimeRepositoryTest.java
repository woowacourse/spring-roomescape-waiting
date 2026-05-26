package integration.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.DuplicateEntityException;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.dto.TimeSlotProjection;

class ReservationTimeRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationTimeDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
    }

    @Test
    void 시간을_저장하고_ID로_조회한다() {
        // given
        LocalTime reservationStartTime = LocalTime.of(10, 0);
        ReservationTime time = new ReservationTime(reservationStartTime);

        // when
        ReservationTime saved = reservationTimeRepository.save(time);

        // then
        Optional<ReservationTime> found = reservationTimeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStartAt()).isEqualTo(reservationStartTime);
    }

    @Test
    void 같은_시간으로_저장하면_참조_무결성_예외가_발생한다() {
        // given
        LocalTime reservationStartTime = LocalTime.of(10, 0);
        ReservationTime time = new ReservationTime(reservationStartTime);
        reservationTimeRepository.save(time);

        // when & then: 무결성 위반 예외를 비즈니스 예외로 변경
        assertThatThrownBy(() -> reservationTimeRepository.save(time))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 존재하는 시간 정보입니다.");
    }

    @Test
    void 특정_시간이_존재하는지_확인한다() {
        // given
        LocalTime targetTime = LocalTime.of(10, 0);
        reservationTimeRepository.save(new ReservationTime(targetTime));

        // when & then
        LocalTime otherTime = LocalTime.of(11, 0);
        assertThat(reservationTimeRepository.existsByStartAt(targetTime)).isTrue();
        assertThat(reservationTimeRepository.existsByStartAt(otherTime)).isFalse();
    }

    @Test
    void 모든_시간_목록을_조회한다() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));

        // when
        List<ReservationTime> times = reservationTimeRepository.findAllTimes();

        // then
        assertThat(times).hasSize(2);
    }

    @Test
    void 테마별로_시간대와_예약_가능_여부를_조회한다() {
        // given: 10시부터 18시까지 한시간 단위로 시간 정보가 존재 + 5월 8일자로 1번 테마에 하나의 예약 존재
        long themeId = 1L;
        LocalDate reservationDate = LocalDate.of(2026, 5, 8);
        dataSource.insertOneTheme();
        dataSource.insertTimeByStartToEndWithOneHourLotation(10, 18);
        dataSource.insertReservation(themeId, reservationDate, 1L);

        // when: 5월 8일 날짜로 1번 테마의 예약 가능한 시간 정보 조회
        List<TimeSlotProjection> result =
                reservationTimeRepository.findTimesByThemeWithReservationStatus(1L,  reservationDate);

        // then: 총 시간 오름차순이 적용된 9개의 타임 슬롯 중 1개만 예약 불가능한 상태
        assertThat(result).hasSize(9);
        assertThat(result)
                .extracting(TimeSlotProjection::startAt)
                .isSorted();

        TimeSlotProjection firstSlot = result.getFirst();
        assertThat(firstSlot.isReservable()).isFalse();
        assertThat(result).filteredOn(TimeSlotProjection::isReservable).hasSize(8);
    }
}
