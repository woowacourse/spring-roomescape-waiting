package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationRepository;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeServiceTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("시간을 삭제한다")
    void test1() {
        // given
        ReservationTimeRegisterDto request = new ReservationTimeRegisterDto(LocalTime.of(15, 0).toString());

        // when
        ReservationTimeResponseDto resaponse = reservationTimeService.saveTime(request);

        // then
        assertThat(resaponse.id()).isNotNull();
        assertThat(resaponse.startAt()).isEqualTo(LocalTime.of(15, 0));
    }

    @Test
    @DisplayName("모든 시간을 조회한다")
    void test2() {
        // when
        List<ReservationTimeResponseDto> times = reservationTimeService.getAllTimes();

        // then
        assertThat(times).hasSize(3);
        assertThat(times).extracting("startAt")
                .containsExactlyInAnyOrder(
                        LocalTime.of(10, 0),
                        LocalTime.of(14, 0),
                        LocalTime.of(18, 0));
    }

    @Test
    @DisplayName("시간을 삭제한다")
    void test3() {
        // given
        ReservationTimeResponseDto saved = reservationTimeService.saveTime(
                new ReservationTimeRegisterDto(LocalTime.of(15, 0).toString())
        );

        // when
        reservationTimeService.deleteTime(saved.id());

        // then
        List<LocalTime> times = reservationTimeService.getAllTimes().stream()
                .map(ReservationTimeResponseDto::startAt)
                .toList();

        assertThat(times).doesNotContain(LocalTime.of(15, 0));
    }

    @Test
    @DisplayName("시간을 삭제할 때 연관된 Reservation 의 ReservationTime 은 모두 null 이 된다")
    void test4() {
        // given
        ReservationTimeResponseDto saved = reservationTimeService.saveTime(
                new ReservationTimeRegisterDto(LocalTime.of(15, 0).toString())
        );

        // when
        reservationTimeService.deleteTime(saved.id());

        // then
        List<ReservationTime> actual = reservationRepository.findByReservationTimeId(saved.id()).stream()
                .map(Reservation::getReservationTime)
                .toList();
        assertThat(actual).allMatch(Objects::isNull);
    }

}
