package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.dto.TimeCreateDto;
import roomescape.application.dto.TimeDto;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.TimeRepository;
import roomescape.testRepository.FakeTimeRepository;

class TimeServiceTest {

    private TimeRepository timeRepository;
    private TimeService timeService;

    @BeforeEach
    void setUp() {
        timeRepository = new FakeTimeRepository();
        timeService = new TimeService(timeRepository);
    }

    @DisplayName("현재 등록되어있는 모든 시간을 조회한다.")
    @Test
    void getAllTimes() {
        // given
        LocalTime time1 = LocalTime.of(10, 0);
        LocalTime time2 = LocalTime.of(11, 0);
        LocalTime time3 = LocalTime.of(12, 0);

        timeRepository.save(ReservationTime.of(1L, time1));
        timeRepository.save(ReservationTime.of(2L, time2));
        timeRepository.save(ReservationTime.of(3L, time3));

        // when
        List<TimeDto> allTimes = timeService.getAllTimes();

        // then
        assertAll(
                () -> assertThat(allTimes).hasSize(3),
                () -> assertThat(allTimes)
                        .extracting(TimeDto::startAt)
                        .containsExactly(time1, time2, time3)
        );
    }

    @DisplayName("요청을 받아 새로운 시간을 등록한다.")
    @Test
    void registerNewTime_withRequest() {
        // given
        LocalTime time = LocalTime.of(10, 0);
        TimeCreateDto timeCreateDto = new TimeCreateDto(time);

        // when
        TimeDto timeDto = timeService.registerNewTime(timeCreateDto);

        // then
        assertAll(
                () -> assertThat(timeDto.id()).isEqualTo(1L),
                () -> assertThat(timeDto.startAt()).isEqualTo(time)
        );
    }

    @DisplayName("등록된 시간을 id로 제거한다.")
    @Test
    void deleteTime() {
        // given
        timeRepository.save(ReservationTime.of(1L, LocalTime.of(10, 0)));
        assertThat(timeRepository.findAll()).hasSize(1);

        // when
        timeService.deleteTime(1L);

        // then
        assertThat(timeRepository.findAll()).isEmpty();
    }

    @DisplayName("id로 ReservationTime 객체를 가져올 수 있다.")
    @Test
    void getReservationTime_byId() {
        // given
        timeRepository.save(ReservationTime.of(1L, LocalTime.of(10, 0)));

        // when
        ReservationTime reservationTime = timeService.getTimeById(1L).toEntity();

        // then
        assertAll(
                () -> assertThat(reservationTime.getId()).isEqualTo(1L),
                () -> assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0))
        );
    }

    @DisplayName("존재하지 않는 id로 객체를 가져오려고 할 때 예외가 발생한다.")
    @Test
    void getReservationTime_fail_when_nonExistId() {
        // then
        Long id = 1L;

        assertThatThrownBy(() -> timeService.getTimeById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("찾으려는 id가 존재하지 않습니다. id: " + id);
    }
}
