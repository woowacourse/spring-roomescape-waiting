package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static roomescape.fixture.MemberFixture.MEMBER_PK;
import static roomescape.fixture.ThemeFixture.TEST_THEME;
import static roomescape.fixture.TimeFixture.ELEVEN_AM;
import static roomescape.fixture.TimeFixture.TEN_AM;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.ServiceTest;
import roomescape.application.reservation.dto.request.ReservationTimeRequest;
import roomescape.application.reservation.dto.response.ReservationTimeResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@ServiceTest
class ReservationTimeServiceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private Clock clock;

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void shouldReturnReservationTimeResponseWhenCreateReservationTime() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.parse("10:00"));
        reservationTimeService.create(reservationTimeRequest);
        List<ReservationTime> times = reservationTimeRepository.findAll();
        assertThat(times).hasSize(1);
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간을 생성 요청하면 예외가 발생한다.")
    void shouldThrowsIllegalStateExceptionWhenCreateExistStartAtTime() {
        LocalTime startAt = reservationTimeRepository.save(TEN_AM.create())
                .getStartAt();
        ReservationTimeRequest request = new ReservationTimeRequest(startAt);
        assertThatCode(() -> reservationTimeService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 시간입니다.");
    }

    @Test
    @DisplayName("예약 시간 조회를 요청하면 저장되어있는 모든 예약 시간대를 반환한다.")
    void shouldReturnAllReservationTimesWhenFindAll() {
        ReservationTime ten = reservationTimeRepository.save(TEN_AM.create());
        ReservationTime eleven = reservationTimeRepository.save(ELEVEN_AM.create());
        List<ReservationTimeResponse> times = reservationTimeService.findAll();
        assertThat(times).containsExactly(
                new ReservationTimeResponse(ten.getId(), ten.getStartAt()),
                new ReservationTimeResponse(eleven.getId(), eleven.getStartAt())
        );
    }

    @Test
    @DisplayName("예약 삭제 요청을 하면, 해당 예약이 저장되어있는지 확인 후 존재하면 삭제한다.")
    void shouldDeleteReservationWhenDeleteById() {
        ReservationTime reservationTime = reservationTimeRepository.save(TEN_AM.create());
        reservationTimeService.deleteById(reservationTime.getId());
        assertThat(reservationTimeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("예약에 사용된 예약 시간을 삭제 요청하면, 예외가 발생한다.")
    void shouldThrowsExceptionReservationWhenReservedInTime() {
        Member member = memberRepository.save(MEMBER_PK.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        ReservationTime time = reservationTimeRepository.save(TEN_AM.create());
        LocalDate date = LocalDate.parse("2024-01-01");
        LocalDateTime createdAt = LocalDateTime.now(clock);
        long timeId = time.getId();

        reservationRepository.save(
                new Reservation(member, theme, date, time, createdAt, BookStatus.BOOKED)
        );
        assertThatCode(() -> reservationTimeService.deleteById(timeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("연관된 예약이 존재하여 삭제할 수 없습니다.");
    }
}
