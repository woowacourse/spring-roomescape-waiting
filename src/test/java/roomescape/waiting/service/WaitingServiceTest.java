package roomescape.waiting.service;

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
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.service.dto.WaitingAddCommand;
import roomescape.waiting.service.dto.WaitingInfo;

public class WaitingServiceTest {

    WaitingRepository waitingRepository;
    WaitingService waitingService;
    ReservationRepository reservationRepository;
    ThemeRepository themeRepository;
    MemberRepository memberRepository;
    CurrentDateTime currentDateTime;

    Member savedMember1;
    Member savedMember2;
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
        savedMember1 = memberRepository.save(new Member(null, "유저1", "이메일", "비밀번호", MemberRole.USER));
        savedMember2 = memberRepository.save(new Member(null, "유저2", "이메일", "비밀번호", MemberRole.USER));
    }

    @DisplayName("과거에 대한 예약 대기를 요청하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenAddWaitingToPastDate() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 1);
        long timeId = savedReservationTime.getId();
        long themeId = savedTheme.getId();
        long memberId = savedMember1.getId();
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
        long memberId = savedMember1.getId();
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
                        savedMember1,
                        currentDateTime.getDate().plusDays(1),
                        savedReservationTime,
                        savedTheme
                )
        );
        LocalDate date = LocalDate.of(2025, 5, 1);
        long timeId = savedReservationTime.getId();
        long themeId = savedTheme.getId();
        long memberId = savedMember2.getId();
        WaitingAddCommand command = new WaitingAddCommand(date, timeId, themeId, memberId);

        // when
        WaitingInfo result = waitingService.addWaiting(command);

        // then
        assertAll(
                () -> assertThat(result.date()).isEqualTo(date),
                () -> assertThat(result.theme().id()).isEqualTo(themeId),
                () -> assertThat(result.time().id()).isEqualTo(timeId),
                () -> assertThat(result.member().id()).isEqualTo(memberId),
                () -> assertThat(result.order()).isEqualTo(1L)
        );
    }

    @DisplayName("같은 멤버가 예약 대기 중 중복 대기를 요청하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenAddWaitingWithDuplicateWaiting() {
        // given
        reservationRepository.save(
                new Reservation(null,
                        savedMember1,
                        currentDateTime.getDate().plusDays(1),
                        savedReservationTime,
                        savedTheme
                )
        );
        LocalDate date = LocalDate.of(2025, 5, 1);
        long timeId = savedReservationTime.getId();
        long themeId = savedTheme.getId();
        long memberId = savedMember2.getId();
        WaitingAddCommand command = new WaitingAddCommand(date, timeId, themeId, memberId);
        waitingService.addWaiting(command);

        // when
        // then
        assertThatThrownBy(() -> waitingService.addWaiting(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 대기 중입니다.");
    }

    @DisplayName("같은 멤버가 예약 중 중복 대기를 요청하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenAddWaitingWithDuplicateReservation() {
        // given
        reservationRepository.save(
                new Reservation(null,
                        savedMember1,
                        currentDateTime.getDate().plusDays(1),
                        savedReservationTime,
                        savedTheme
                )
        );
        LocalDate date = LocalDate.of(2025, 5, 1);
        long timeId = savedReservationTime.getId();
        long themeId = savedTheme.getId();
        long memberId = savedMember1.getId();
        WaitingAddCommand command = new WaitingAddCommand(date, timeId, themeId, memberId);

        // when
        // then
        assertThatThrownBy(() -> waitingService.addWaiting(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 예약되었습니다.");
    }

    @DisplayName("예약한 멤버와 다른 멤버가 대기를 취소하면 예외가 발생한다")
    @Test
    void should_ThrowException_WhenCancelWaitingWithDifferentMember() {
        // given
        LocalDate tomorrow = currentDateTime.getDate().plusDays(1);
        Waiting waiting = new Waiting(null, tomorrow, savedReservationTime, savedTheme, savedMember1, 1L);
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        // then
        assertThatThrownBy(() -> waitingService.cancelById(savedWaiting.getId(), savedMember2.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 대기에 대한 취소 권한이 없습니다.");
    }

    @DisplayName("자신의 예약 대기를 취소할 수 있다.")
    @Test
    void cancelWaiting() {
        // given
        LocalDate tomorrow = currentDateTime.getDate().plusDays(1);
        Waiting waiting = new Waiting(null, tomorrow, savedReservationTime, savedTheme, savedMember1, 1L);
        Waiting savedWaiting = waitingRepository.save(waiting);

        // when
        waitingService.cancelById(savedWaiting.getId(), savedMember1.getId());

        // then
        boolean exists = waitingRepository.existsByIdAndMemberId(savedWaiting.getId(), savedMember1.getId());
        assertThat(exists).isFalse();
    }
}
