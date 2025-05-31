package roomescape.unit.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.datetime.CurrentDateTime;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.support.fake.FakeMemberRepository;
import roomescape.support.fake.FakeReservationRepository;
import roomescape.support.fake.FakeThemeRepository;
import roomescape.support.fake.FakeTimeSlotRepository;
import roomescape.support.fake.FakeWaitingRepository;
import roomescape.support.util.TestCurrentDateTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotRepository;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.application.dto.WaitingInfo;
import roomescape.waiting.application.service.WaitingService;
import roomescape.waiting.domain.WaitingRepository;

public class WaitingServiceTest {

    private static final CurrentDateTime currentDateTime = new TestCurrentDateTime(
            LocalDateTime.of(2025, 4, 2, 11, 0));
    private static final LocalDate today = currentDateTime.getDate();
    private final LocalDate yesterday = today.minusDays(1);
    private final LocalDate tomorrow = today.plusDays(1);

    private final ReservationRepository reservationRepository = new FakeReservationRepository();
    private final WaitingRepository waitingRepository = new FakeWaitingRepository();
    private final TimeSlotRepository timeSlotRepository = new FakeTimeSlotRepository();
    private final ThemeRepository themeRepository = new FakeThemeRepository();
    private final MemberRepository memberRepository = new FakeMemberRepository();
    private final WaitingService waitingService = new WaitingService(reservationRepository, waitingRepository, memberRepository, currentDateTime);

    private Member member1, member2;
    private TimeSlot time1, time2;
    private Theme theme1, theme2;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(new Member("리버1", "river1@gmail.com", "riverpw1", MemberRole.ADMIN));
        member2 = memberRepository.save(new Member("리버2", "river2@gmail.com", "riverpw2", MemberRole.ADMIN));

        time1 = timeSlotRepository.save(new TimeSlot(LocalTime.of(11, 0)));
        time2 = timeSlotRepository.save(new TimeSlot(LocalTime.of(12, 0)));

        theme1 = themeRepository.save(new Theme("우테코탈출1", "우테코탈출1 설명, ", "우테코탈출1 썸네일.jpg"));
        theme2 = themeRepository.save(new Theme("우테코탈출2", "우테코탈출2 설명, ", "우테코탈출2 썸네일.jpg"));
    }


    @DisplayName("날짜와 시간과 테마가 중복되는 예약 대기를 할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateWaiting() {
        // given
        final WaitingCreateCommand request = new WaitingCreateCommand(tomorrow, member1.id(), time1.id(), theme1.id());
        waitingService.createWaiting(request);
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("해당 시간에 이미 예약 대기가 존재합니다.");
    }

    @DisplayName("날짜와 시간이 같아도 테마가 다른 예약 대기를 할 경우 예외가 발생하지 않는다")
    @Test
    void shouldNot_ThrowException_WhenThemeIsDifferent() {
        // given
        final WaitingCreateCommand request1 = new WaitingCreateCommand(tomorrow, time1.id(), theme1.id(), member1.id());
        final WaitingCreateCommand request2 = new WaitingCreateCommand(tomorrow, time1.id(), theme2.id(), member2.id());
        waitingService.createWaiting(request1);
        // when & then
        assertThatCode(() -> waitingService.createWaiting(request2))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 대기 시간이 존재하지 않을 경우 예외가 발생한다")
    @Test
    void validateTime() {
        // given
        final WaitingCreateCommand request = new WaitingCreateCommand(tomorrow, 3L, theme1.id(), member1.id());
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("예약 시간이 존재하지 않습니다.");
    }

    @DisplayName("테마가 존재하지 않을 경우 예외가 발생한다")
    @Test
    void validateTheme() {
        // given
        final WaitingCreateCommand request = new WaitingCreateCommand(tomorrow, time1.id(), 3L, member1.id());
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("테마가 존재하지 않습니다.");
    }

    @DisplayName("과거 시간에 예약 대기할 경우 예외가 발생한다")
    @Test
    void validatePastTime() {
        // given
        final WaitingCreateCommand request = new WaitingCreateCommand(yesterday, time1.id(), theme1.id(), member1.id());
        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(RoomescapeException.class)
                .hasMessageContaining("지나간 날짜와 시간은 예약 대기할 수 없습니다.");
    }

    @DisplayName("예약 대기를 생성할 수 있다")
    @Test
    void create() {
        // given
        final WaitingCreateCommand request = new WaitingCreateCommand(tomorrow, time1.id(), theme1.id(), member1.id());
        // when
        final WaitingInfo response = waitingService.createWaiting(request);
        // then
        final boolean result = waitingRepository.existsByReservationId(response.reservationInfo().id());
        assertThat(result).isTrue();
    }

    @DisplayName("예약 대기 목록을 조회할 수 있다")
    @Test
    void findWaitings() {
        // given
        final WaitingCreateCommand request1 = new WaitingCreateCommand(tomorrow, time1.id(), theme1.id(), member1.id());
        final WaitingCreateCommand request2 = new WaitingCreateCommand(tomorrow, time2.id(), theme2.id(), member2.id());
        waitingService.createWaiting(request1);
        waitingService.createWaiting(request2);
        // when
        final List<WaitingInfo> result = waitingService.findWaitings();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약 대기를 삭제할 수 있다")
    @Test
    void cancelById() {
        // given
        final WaitingCreateCommand request = new WaitingCreateCommand(tomorrow, time1.id(), theme1.id(), member1.id());
        final WaitingInfo response = waitingService.createWaiting(request);
        // when
        waitingService.cancelWaitingById(response.id());
        // then
        assertThat(waitingService.findWaitings()).isEmpty();
    }
}
