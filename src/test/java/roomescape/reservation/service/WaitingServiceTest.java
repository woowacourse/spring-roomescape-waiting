package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.CurrentDateTime;
import roomescape.fake.FakeMemberRepository;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeThemeRepository;
import roomescape.fake.FakeWaitingRepository;
import roomescape.fake.TestCurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.service.dto.WaitingAddCommand;
import roomescape.reservation.service.dto.WaitingInfo;

public class WaitingServiceTest {

    WaitingRepository waitingRepository;
    WaitingService waitingService;
    ReservationRepository reservationRepository;
    ThemeRepository themeRepository;
    MemberRepository memberRepository;
    CurrentDateTime currentDateTime;

    Member savedMember;
    ReservationTime savedReservationTime;
    Theme savedTheme;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        reservationRepository = new FakeReservationRepository();
        ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();
        currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 4, 30, 10, 0));
        waitingService = new WaitingService(waitingRepository, reservationRepository, reservationTimeRepository,
                themeRepository, memberRepository, currentDateTime);
        savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(new Theme(null, "공포", "무서운거임", "abcd"));
        savedMember = memberRepository.save(new Member(null, "유저1", "이메일", "비밀번호", MemberRole.USER));
    }

    @DisplayName("과거에 대한 예약 대기를 요청하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenAddWaitingToPastDate() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 1);
        long timeId = savedReservationTime.getId();
        long themeId = savedTheme.getId();
        long memberId = savedMember.getId();
        WaitingAddCommand command = new WaitingAddCommand(date, timeId, themeId, memberId);

        // when
        // then
        assertThatThrownBy(() -> waitingService.addWaiting(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지난 날짜 및 시간에는 대기할 수 없습니다.");
    }

    @DisplayName("예약이 존재하지 않는 경우에 예약 대기를 요청하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenAddWaitingToNoReservation() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 1);
        long timeId = savedReservationTime.getId();
        long themeId = savedTheme.getId();
        long memberId = savedMember.getId();
        WaitingAddCommand command = new WaitingAddCommand(date, timeId, themeId, memberId);

        // when
        // then
        assertThatThrownBy(() -> waitingService.addWaiting(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 예약이 존재하지 않아 대기할 수 없습니다.");
    }

    @DisplayName("예약 대기를 요청할 수 있다")
    @Test
    void addWaiting() {
        // given
        reservationRepository.save(
                new Reservation(null,
                        savedMember,
                        currentDateTime.getDate().plusDays(1),
                        savedReservationTime,
                        savedTheme
                )
        );
        LocalDate date = LocalDate.of(2025, 5, 1);
        long timeId = savedReservationTime.getId();
        long themeId = savedTheme.getId();
        long memberId = savedMember.getId();
        WaitingAddCommand command = new WaitingAddCommand(date, timeId, themeId, memberId);

        // when
        WaitingInfo result = waitingService.addWaiting(command);

        // then
        assertAll(
                () -> assertThat(result.date()).isEqualTo(date),
                () -> assertThat(result.themeInfo().id()).isEqualTo(themeId),
                () -> assertThat(result.timeInfo().id()).isEqualTo(timeId),
                () -> assertThat(result.memberInfo().id()).isEqualTo(memberId),
                () -> assertThat(result.order()).isEqualTo(1L)
        );
    }
}
