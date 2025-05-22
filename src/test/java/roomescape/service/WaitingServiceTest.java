package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;
import static roomescape.TestFixture.createDefaultMember;
import static roomescape.TestFixture.createDefaultReservationTime;
import static roomescape.TestFixture.createDefaultTheme;
import static roomescape.TestFixture.createDefaultWaiting_1;
import static roomescape.TestFixture.createDefaultWaiting_2;
import static roomescape.TestFixture.createMemberByName;
import static roomescape.TestFixture.createWaiting;

import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.DBHelper;
import roomescape.DatabaseCleaner;
import roomescape.controller.dto.request.LoginMemberInfo;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.UnAvailableReservationException;
import roomescape.service.dto.param.CreateBookingParam;
import roomescape.service.dto.result.WaitingResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WaitingServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private DBHelper dbHelper;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void clean() {
        databaseCleaner.clean();
    }

    @DisplayName("예약 대기를 생성할 수 있다.")
    @Test
    void canCreateWaiting() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(createDefaultReservationTime());
        Theme theme = themeRepository.save(createDefaultTheme());
        Member member = memberRepository.save(createDefaultMember());
        CreateBookingParam createBookingParam = new CreateBookingParam(member.getId(), DEFAULT_DATE,
                reservationTime.getId(), theme.getId());

        //when
        WaitingResult waitingResult = waitingService.create(createBookingParam);

        //then
        Waiting waiting = waitingRepository.findById(waitingResult.id()).get();
        assertAll(
                () -> assertThat(waiting.getId()).isNotNull(),
                () -> assertThat(waiting.getMember()).isEqualTo(member),
                () -> assertThat(waiting.getTime()).isEqualTo(reservationTime),
                () -> assertThat(waiting.getTheme()).isEqualTo(theme)
        );
    }

    @DisplayName("예약 대기를 생성할 때, 이미 예약이 존재하면 예외가 발생한다.")
    @Test
    void error_createWaiting() {
        //given
        Theme theme = themeRepository.save(createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(createDefaultReservationTime());
        Member member = memberRepository.save(createDefaultMember());

        Waiting waiting = createWaiting(member, DEFAULT_DATE, reservationTime, theme);
        waitingRepository.save(waiting);

        //when & then
        assertThatThrownBy(() -> waitingService.create(
                new CreateBookingParam(member.getId(), DEFAULT_DATE, reservationTime.getId(), theme.getId())
        )).isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("이미 동일한 시간에 대기가 존재합니다.");
    }

    @DisplayName("대기 상태의 예약을 모두 조회할 수 있다.")
    @Test
    void getWaitingReservations() {
        //given
        Waiting waiting1 = createDefaultWaiting_1();
        Waiting waiting2 = createDefaultWaiting_2();
        dbHelper.insertWaiting(waiting1);
        dbHelper.insertWaiting(waiting2);

        //when
        List<WaitingResult> results = waitingService.getAll();

        //then
        assertAll(
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results)
                        .isEqualTo(List.of(
                                WaitingResult.from(waiting1),
                                WaitingResult.from(waiting2)
                        ))
        );
    }

    @DisplayName("대기를 정상적으로 취소할 수 있다")
    @Test
    void cancelWaitingByIdSuccess() {
        //given
        Theme theme = themeRepository.save(createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(createDefaultReservationTime());
        Member member = memberRepository.save(createDefaultMember());

        Waiting waiting = waitingRepository.save(createWaiting(member, DEFAULT_DATE, reservationTime, theme));
        LoginMemberInfo loginMemberInfo = new LoginMemberInfo(member.getId());

        //when
        waitingService.cancelWaitingById(waiting.getId(), loginMemberInfo);

        //then
        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
    }

    @DisplayName("자신의 예약이 아닌 경우 대기 취소할 수 없다")
    @Test
    void cancelWaitingByIdWithoutPermission() {
        //given
        Theme theme = themeRepository.save(createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(createDefaultReservationTime());
        Member member1 = memberRepository.save(createMemberByName("member1"));
        Member member2 = memberRepository.save(createMemberByName("member2"));

        Waiting waiting = waitingRepository.save(createWaiting(member1, DEFAULT_DATE, reservationTime, theme));
        LoginMemberInfo loginMemberInfo = new LoginMemberInfo(member2.getId());

        //when & then
        assertThatThrownBy(() -> waitingService.cancelWaitingById(waiting.getId(), loginMemberInfo))
                .isInstanceOf(DeletionNotAllowedException.class)
                .hasMessage("자신의 예약만 삭제할 수 있습니다.");
    }

    @Test
    void denyWaitingById() {
    }
}
