package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Role;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.AvailableTimeRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.response.AvailableTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("예약 시간 추가 테스트")
    @Test
    void createReservationTime() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 1));

        ReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(reservationTimeRequest);

        assertThat(reservationTime.startAt()).isEqualTo(LocalTime.of(10, 1));
    }

    @DisplayName("모든 예약 시간 조회 테스트")
    @Test
    void findAllReservationTimes() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 1)));

        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAllReservationTimes();
        assertThat(reservationTimes).hasSize(1);
    }

    @DisplayName("예약 시간 삭제 테스트")
    @Test
    void deleteReservationTime() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 1)));

        reservationTimeService.deleteReservationTime(reservationTime.getId());

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        assertThat(reservationTimes).isEmpty();
    }

    @DisplayName("예약 가능한 시간 조회")
    @Test
    void findAvailableTimes() {
        LocalDate searchDate = LocalDate.of(2999, 12, 12);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(12, 0));
        Theme theme = new Theme(1L, "happy", "hi", "abcd.html");

        ReservationTime reservedTime = reservationTimeRepository.save(time1);
        ReservationTime nonReservedTime = reservationTimeRepository.save(time2);
        Theme savedTheme = themeRepository.save(theme);

        reservationRepository.save(new Reservation(
                new Member(1L, "asd", "asd@email.com", "password", Role.USER),
                searchDate, reservedTime, savedTheme
        ));

        List<AvailableTimeResponse> availableTimes = reservationTimeService.findAvailableTimes(
                new AvailableTimeRequest(savedTheme.getId(), searchDate));

        assertThat(availableTimes).containsExactly(
                AvailableTimeResponse.of(reservedTime, false),
                AvailableTimeResponse.of(nonReservedTime, true)
        );
    }

}
