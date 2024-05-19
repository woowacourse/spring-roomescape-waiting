package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

@SpringBootTest
@Transactional
class AvailableTimeServiceTest {

    @Autowired
    private AvailableTimeService availableTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("날짜와 테마, 시간에 대한 예약 내역을 확인할 수 있다.")
    void findAvailableTimeTest() {
        //given
        Member member = memberRepository.save(DEFAULT_MEMBER);
        Theme theme = themeRepository.save(DEFAULT_THEME);
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        ReservationTime time3 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        ReservationTime time4 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));

        LocalDate selectedDate = LocalDate.now().plusDays(1);
        reservationRepository.save(new Reservation(member, selectedDate, time1, theme));
        reservationRepository.save(new Reservation(member, selectedDate, time3, theme));

        //when
        List<AvailableTimeResponse> availableTimeResponses = availableTimeService.findByThemeAndDate(selectedDate, theme.getId());

        //then
        assertThat(availableTimeResponses).containsExactlyInAnyOrder(
                new AvailableTimeResponse(time1.getId(), time1.getStartAt(), true),
                new AvailableTimeResponse(time2.getId(), time2.getStartAt(), false),
                new AvailableTimeResponse(time3.getId(), time3.getStartAt(), true),
                new AvailableTimeResponse(time4.getId(), time4.getStartAt(), false)
        );
    }
}
