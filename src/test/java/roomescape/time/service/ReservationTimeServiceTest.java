package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.time.exception.ReservationTimeErrorInformation.TIME_ALREADY_EXISTS;
import static roomescape.time.fixture.ReservationTimeFixture.saveDto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.time.domain.ReservationTime;
import roomescape.time.controller.dto.request.ReservationTimeSaveDto;
import roomescape.time.exception.ReservationTimeException;
import roomescape.time.fixture.FakeReservationTimeRepository;
import roomescape.time.fixture.ReservationTimeFixture;

class ReservationTimeServiceTest {

    private FakeReservationTimeRepository reservationTimeRepository;
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setup() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationTimeService = new ReservationTimeService(reservationTimeRepository);
    }

    @Test
    @DisplayName("관리자 등록된 시간 조회시, 활성화/비활성화된 시간을 모두 조회한다.")
    void readAll() {
        // given
        List<ReservationTime> times = List.of(
                ReservationTimeFixture.time15(),
                ReservationTimeFixture.activeTime16()
        );
        saveAll(times);

        // when
        List<ReservationTime> actual = reservationTimeService.readAll();

        // then
        assertThat(actual)
                .hasSize(times.size());
    }

    @Test
    @DisplayName("예약 시간을 추가한다.")
    void register() {
        // given
        List<ReservationTime> times = List.of();

        // when
        reservationTimeService.register(saveDto(LocalTime.of(12, 0)));

        // then
        assertThat(reservationTimeService.readAll())
                .hasSize(times.size() + 1);
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간 생성 시 예외가 발생한다.")
    void register_already_exist() {
        // given
        ReservationTime saved = save(ReservationTimeFixture.time15());
        ReservationTimeSaveDto duplicatedCommand = saveDto(saved.getStartAt());

        // when & then
        assertThatThrownBy(() -> reservationTimeService.register(duplicatedCommand))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(TIME_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("등록된 시간을 비활성화할 수 있다.")
    void updateStatus_inactive() {
        // given
        ReservationTime saved = reservationTimeRepository.save(ReservationTimeFixture.activeTime15());

        // when
        reservationTimeService.updateStatus(saved.getId(), false);

        // then
        assertThat(reservationTimeRepository.findById(saved.getId()).get().isActive())
                .isFalse();
    }

    @Test
    @DisplayName("등록된 시간을 활성화할 수 있다.")
    void updateStatus_active() {
        // given
        ReservationTime saved = reservationTimeRepository.save(ReservationTimeFixture.activeTime15());

        // when
        reservationTimeService.updateStatus(saved.getId(), true);

        // then
        assertThat(reservationTimeRepository.findById(saved.getId()).get().isActive())
                .isTrue();
    }

    private List<ReservationTime> saveAll(List<ReservationTime> reservationTimes) {
        List<ReservationTime> savedReservationTimes = new ArrayList<>();
        for (ReservationTime reservationTime : reservationTimes) {
            savedReservationTimes.add(save(reservationTime));
        }
        return savedReservationTimes;
    }

    private ReservationTime save(ReservationTime reservationTime) {
        return reservationTimeRepository.save(reservationTime);
    }

}
