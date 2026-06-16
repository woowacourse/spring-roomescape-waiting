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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.date.repository.ReservationDateRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.controller.dto.request.ReservationTimeSaveDto;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.ReservationTimeException;
import roomescape.time.fixture.ReservationTimeFixture;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest
class ReservationTimeServiceTest {

    @Autowired
    private ReservationDateRepository reservationDateRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setup() {
        reservationTimeService = new ReservationTimeService(reservationDateRepository,
            reservationTimeRepository, themeRepository);
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


    @Nested
    @DisplayName("readAll 메서드는")
    class ReadAllTest {


        @Test
        @DisplayName("모든 시간을 조회한다")
        void 성공() {
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
    }

    @Nested
    @DisplayName("register 메서드는")
    class RegisterTest {


        @Test
        @DisplayName("예약 시간을 등록한다")
        void 성공() {
            // given
            List<ReservationTime> times = List.of();

            // when
            reservationTimeService.register(saveDto(LocalTime.of(12, 0)));

            // then
            assertThat(reservationTimeService.readAll())
                .hasSize(times.size() + 1);
        }


        @Test
        @DisplayName("이미 존재하는 시간이면 예외가 발생한")
        void 실패() {
            // given
            ReservationTime saved = save(ReservationTimeFixture.time15());
            ReservationTimeSaveDto duplicatedCommand = saveDto(saved.getStartAt());

            // when & then
            assertThatThrownBy(() -> reservationTimeService.register(duplicatedCommand))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(TIME_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    @DisplayName("updateStatus 메서드는")
    class UpdateStatusTest {


        @Test
        @DisplayName("상태를 비활성화로 변경한다")
        void 성공1() {
            // given
            ReservationTime saved = reservationTimeRepository.save(
                ReservationTimeFixture.activeTime15());

            // when
            reservationTimeService.updateStatus(saved.getId(), false);

            // then
            assertThat(reservationTimeRepository.findById(saved.getId()).get().isActive())
                .isFalse();
        }


        @Test
        @DisplayName("상태를 활성화로 변경한다")
        void 성공2() {
            // given
            ReservationTime saved = reservationTimeRepository.save(
                ReservationTimeFixture.activeTime15());

            // when
            reservationTimeService.updateStatus(saved.getId(), true);

            // then
            assertThat(reservationTimeRepository.findById(saved.getId()).get().isActive())
                .isTrue();
        }
    }
}
