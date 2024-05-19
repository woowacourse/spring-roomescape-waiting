package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.AvailableTimeResponse;
import roomescape.repository.CollectionReservationRepository;
import roomescape.repository.CollectionReservationTimeRepository;
import roomescape.repository.CollectionThemeRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

class AvailableTimeServiceTest {

    private AvailableTimeService availableTimeService;
    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;

    @BeforeEach
    void init() {
        reservationRepository = new CollectionReservationRepository();
        reservationTimeRepository = new CollectionReservationTimeRepository(reservationRepository);
        themeRepository = new CollectionThemeRepository();
        availableTimeService = new AvailableTimeService(reservationTimeRepository, themeRepository);
    }

    @Test
    @DisplayName("날짜와 테마, 시간에 대한 예약 내역을 확인할 수 있다.")
    void findAvailableTimeTest() {
        //given
        themeRepository.save(DEFAULT_THEME);
        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        ReservationTime reservationTime3 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        ReservationTime reservationTime4 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));

        LocalDate selectedDate = LocalDate.of(2024, 1, 1);
        reservationRepository.save(new Reservation(DEFAULT_MEMBER, selectedDate, reservationTime1, DEFAULT_THEME));
        reservationRepository.save(new Reservation(DEFAULT_MEMBER, selectedDate, reservationTime3, DEFAULT_THEME));

        //when
        List<AvailableTimeResponse> availableTimeResponses = availableTimeService.findByThemeAndDate(selectedDate,
                DEFAULT_THEME.getId());

        //then
        assertThat(availableTimeResponses).containsExactlyInAnyOrder(
                new AvailableTimeResponse(1L, reservationTime1.getStartAt(), true),
                new AvailableTimeResponse(2L, reservationTime2.getStartAt(), false),
                new AvailableTimeResponse(3L, reservationTime3.getStartAt(), true),
                new AvailableTimeResponse(4L, reservationTime4.getStartAt(), false)
        );
    }
}
