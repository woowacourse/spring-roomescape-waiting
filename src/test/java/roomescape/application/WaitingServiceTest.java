package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static roomescape.testFixture.Fixture.GAME_SCHEDULE_1;
import static roomescape.testFixture.Fixture.GAME_SCHEDULE_2;
import static roomescape.testFixture.Fixture.GAME_SCHEDULE_3;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.MEMBER2_USER;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationStatusServiceResponse;
import roomescape.application.dto.WaitingServiceResponse;
import roomescape.domain.ReservationStatus;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;
import roomescape.domain.entity.Waiting;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.NotFoundException;
import roomescape.testFixture.Fixture;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @InjectMocks
    private WaitingService service;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private GameScheduleService gameScheduleService;

    @Mock
    private MemberService memberService;

    @Mock
    private MessageSource messageSource;

    @Test
    @DisplayName("중복이 아닌 경우 예약 대기를 등록한다")
    void registerWaiting_success() {
        // given
        Member member = Fixture.MEMBER1_ADMIN;
        Theme theme = Fixture.THEME_1;
        ReservationTime time = Fixture.RESERVATION_TIME_1;
        LocalDate date = Fixture.TOMORROW;
        GameSchedule gameSchedule = GAME_SCHEDULE_1;

        doReturn(gameSchedule).when(gameScheduleService).getGameScheduleEntityBy(date, time.getId(), theme.getId());
        doReturn(member).when(memberService).getMemberEntityById(member.getId());

        ArgumentCaptor<Waiting> captor = ArgumentCaptor.forClass(Waiting.class);
        Waiting waiting = Waiting.withId(1L, member, gameSchedule, ReservationStatus.WAITING);
        doReturn(waiting).when(waitingRepository).save(any(Waiting.class));

        ReservationCreateServiceRequest request = new ReservationCreateServiceRequest(
                date,
                time.getId(),
                theme.getId(),
                member.getId()
        );

        // when
        WaitingServiceResponse response = service.registerWaiting(request);

        // then
        verify(waitingRepository).save(captor.capture());
        Waiting capturedWaiting = captor.getValue();
        assertAll(
                () -> assertThat(capturedWaiting.getMember()).isEqualTo(waiting.getMember()),
                () -> assertThat(capturedWaiting.getGameSchedule()).isEqualTo(waiting.getGameSchedule()),
                () -> assertThat(capturedWaiting.getStatus()).isEqualTo(waiting.getStatus()),
                () -> assertThat(response.id()).isEqualTo(waiting.getId()),
                () -> assertThat(response.member().id()).isEqualTo(waiting.getMember().getId()),
                () -> assertThat(response.theme().id()).isEqualTo(waiting.getGameSchedule().getTheme().getId()),
                () -> assertThat(response.time().id()).isEqualTo(waiting.getGameSchedule().getTime().getId())
        );
    }

    @Test
    @DisplayName("이미 예약대기한 게임 스케줄에 대해 중복 예약대기를 할 수 없다")
    void registerWaiting_Duplicate() {
        // given
        Member member = Fixture.MEMBER1_ADMIN;
        Theme theme = Fixture.THEME_1;
        ReservationTime time = Fixture.RESERVATION_TIME_1;
        LocalDate date = Fixture.TOMORROW;
        GameSchedule gameSchedule = GAME_SCHEDULE_1;

        doReturn(gameSchedule).when(gameScheduleService).getGameScheduleEntityBy(date, time.getId(), theme.getId());
        doReturn(member).when(memberService).getMemberEntityById(member.getId());
        doReturn(true).when(waitingRepository).existsByGameScheduleIdAndMemberId(gameSchedule.getId(), member.getId());

        ReservationCreateServiceRequest request = new ReservationCreateServiceRequest(
                date,
                time.getId(),
                theme.getId(),
                member.getId()
        );

        // when & then
        assertThatThrownBy(() -> service.registerWaiting(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약대기는 한 번만 신청할 수 있습니다.");
    }

    @Test
    @DisplayName("모든 예약대기 목록을 조회한다")
    void getAllWaitings() {
        // given
        Waiting waiting1 = Waiting.withId(1L, MEMBER1_ADMIN, GAME_SCHEDULE_1, ReservationStatus.WAITING);
        Waiting waiting2 = Waiting.withId(2L, MEMBER1_ADMIN, GAME_SCHEDULE_2, ReservationStatus.WAITING);
        Waiting waiting3 = Waiting.withId(3L, MEMBER1_ADMIN, GAME_SCHEDULE_3, ReservationStatus.WAITING);
        Waiting waiting4 = Waiting.withId(4L, MEMBER2_USER, Fixture.GAME_SCHEDULE_4, ReservationStatus.WAITING);
        Waiting waiting5 = Waiting.withId(5L, MEMBER2_USER, GAME_SCHEDULE_1, ReservationStatus.WAITING);
        doReturn(List.of(waiting1, waiting2, waiting3, waiting4, waiting5)).when(waitingRepository).findAll();

        // when
        List<WaitingServiceResponse> responses = service.getAllWaitings();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(5),
                () -> assertThat(responses.getFirst().id()).isEqualTo(waiting1.getId()),
                () -> assertThat(responses.getFirst().member().id()).isEqualTo(waiting1.getMember().getId()),
                () -> assertThat(responses.getLast().id()).isEqualTo(waiting5.getId()),
                () -> assertThat(responses.getLast().member().id()).isEqualTo(waiting5.getMember().getId())
        );
    }

    @Test
    @DisplayName("회원의 예약대기 목록을 조회한다")
    void getWaitingsByMember() {
        // given
        Waiting waiting1 = Waiting.withId(1L, MEMBER2_USER, GAME_SCHEDULE_1, ReservationStatus.WAITING);
        Waiting waiting2 = Waiting.withId(2L, MEMBER1_ADMIN, GAME_SCHEDULE_1, ReservationStatus.WAITING);
        Waiting waiting3 = Waiting.withId(3L, MEMBER1_ADMIN, GAME_SCHEDULE_2, ReservationStatus.WAITING);
        Waiting waiting4 = Waiting.withId(4L, MEMBER1_ADMIN, GAME_SCHEDULE_3, ReservationStatus.WAITING);

        List<Waiting> memberWaitings = List.of(waiting2, waiting3, waiting4);
        doReturn(memberWaitings).when(waitingRepository).findByMemberId(MEMBER1_ADMIN.getId());

        List<Waiting> sameScheduleWaitings = List.of(waiting1, waiting2);
        doReturn(sameScheduleWaitings).when(waitingRepository).findByGameSchedule(GAME_SCHEDULE_1);
        doReturn(List.of(waiting3)).when(waitingRepository).findByGameSchedule(GAME_SCHEDULE_2);
        doReturn(List.of(waiting4)).when(waitingRepository).findByGameSchedule(GAME_SCHEDULE_3);

        when(messageSource.getMessage(eq("WAITING"), any(), eq(Locale.KOREA)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1);
                    return args[0] + "예약대기";
                });

        // when
        List<ReservationStatusServiceResponse> responses = service.getWaitingsByMember(MEMBER1_ADMIN.getId());

        // then
        assertAll(
                () -> assertThat(responses).hasSize(3),
                () -> assertThat(responses.get(0).reservationId()).isEqualTo(waiting2.getId()),
                () -> assertThat(responses.get(0).status()).contains("2번째"),
                () -> assertThat(responses.get(1).reservationId()).isEqualTo(waiting3.getId()),
                () -> assertThat(responses.get(1).status()).contains("1번째"),
                () -> assertThat(responses.get(2).reservationId()).isEqualTo(waiting4.getId()),
                () -> assertThat(responses.get(2).status()).contains("1번째")
        );
    }

    @Test
    @DisplayName("게임 스케줄의 첫 번째 예약대기를 조회한다")
    void findFirstWaitingEntityByGameSchedule() {
        // given
        Waiting waiting1 = Waiting.withId(1L, MEMBER1_ADMIN, GAME_SCHEDULE_1, ReservationStatus.WAITING);
        Waiting waiting2 = Waiting.withId(2L, MEMBER2_USER, GAME_SCHEDULE_1, ReservationStatus.WAITING);
        doReturn(List.of(waiting1, waiting2)).when(waitingRepository).findByGameSchedule(GAME_SCHEDULE_1);

        // when
        Optional<Waiting> actual = service.findFirstWaitingEntityByGameSchedule(GAME_SCHEDULE_1);

        // then
        assertAll(
                () -> assertThat(actual).isPresent(),
                () -> assertThat(actual.get()).isEqualTo(waiting1)
        );
    }

    @Test
    @DisplayName("예약대기를 삭제한다")
    void deleteWaiting() {
        // given
        Waiting waiting = Waiting.withId(1L, MEMBER1_ADMIN, GAME_SCHEDULE_1, ReservationStatus.WAITING);
        doNothing().when(waitingRepository).deleteById(waiting.getId());

        // when & then
        assertAll(
                () -> assertThatCode(() -> service.deleteWaiting(waiting.getId())).doesNotThrowAnyException(),
                () -> verify(waitingRepository).deleteById(waiting.getId())
        );
    }

    @Test
    @DisplayName("존재하지 않는 예약대기를 삭제할 수 없다")
    void deleteWaiting_NotFound() {
        // given
        Long notFoundId = 999L;
        doThrow(new EmptyResultDataAccessException(1)).when(waitingRepository).deleteById(notFoundId);

        // when & then
        assertThatThrownBy(() -> service.deleteWaiting(notFoundId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("삭제하려는 예약대기 id가 존재하지 않습니다. id: " + notFoundId);
    }
} 
