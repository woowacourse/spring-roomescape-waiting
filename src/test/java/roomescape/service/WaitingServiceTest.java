package roomescape.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.TestFixture;
import roomescape.domain.*;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.service.param.CreateWaitingParam;
import roomescape.service.result.WaitingResult;
import roomescape.service.result.WaitingWithRankResult;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.TEST_DATE;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WaitingServiceTest {

    @MockitoSpyBean
    private WaitingService waitingService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약 대기를 생성할 수 있다.")
    void create() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        CreateWaitingParam createWaitingParam = new CreateWaitingParam(member.getId(),
                TestFixture.TEST_DATE,
                reservationTime.getId(),
                theme.getId());
        //when
        WaitingResult waitingResult = waitingService.create(createWaitingParam);

        //then
        Waiting waiting = waitingRepository.findById(waitingResult.id()).get();
        assertThat(waiting.getId()).isNotNull();
    }

    @Test
    @DisplayName("예약 대기 전체 조회를 할 수 있다.")
    void findAll() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Waiting waiting1 = waitingRepository.save(TestFixture.createWaiting(member, TEST_DATE, reservationTime, theme));
        Waiting waiting2 = waitingRepository.save(TestFixture.createWaiting(member, TEST_DATE.plusDays(1), reservationTime, theme));

        //when
        List<WaitingResult> waitingResults = waitingService.findAll();

        //then
        assertAll(
                () -> assertThat(waitingResults).hasSize(2),
                () -> assertThat(waitingResults.getFirst())
                        .isEqualTo(WaitingResult.from(waiting1))
        );
    }

    @Test
    @DisplayName("사용자 ID를 통해 예약 대기와 대기 순번을 찾을 수 있다.")
    void findWaitingsWithRankByMemberId() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Waiting waiting1 = waitingRepository.save(TestFixture.createWaiting(member, TEST_DATE, reservationTime, theme));
        Waiting waiting2 = waitingRepository.save(TestFixture.createWaiting(member, TEST_DATE.plusDays(1), reservationTime, theme));

        //when
        List<WaitingWithRankResult> waitingWithRankResults = waitingService.findWaitingsWithRankByMemberId(member.getId());

        //then
        assertAll(
                () -> assertThat(waitingWithRankResults).hasSize(2),
                () -> assertThat(waitingWithRankResults.getFirst().rank()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다.")
    void deleteByMemberIdAndWaitingId() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Waiting waiting = waitingRepository.save(TestFixture.createWaiting(member, TEST_DATE, reservationTime, theme));

        //when & then
        assertThatCode(
                () -> waitingService.deleteByMemberIdAndWaitingId(member.getId(), waiting.getId())
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기를 한 사용자와 삭제를 한 사용자가 다르면 예외를 던진다.")
    void deleteByMemberIdAndWaitingId2() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member1 = memberRepository.save(TestFixture.createMemberByName("moru"));
        Member member2 = memberRepository.save(TestFixture.createMemberByName("hippo"));
        Waiting waiting = waitingRepository.save(TestFixture.createWaiting(member1, TEST_DATE, reservationTime, theme));

        //when & then
        assertThatThrownBy(
                () -> waitingService.deleteByMemberIdAndWaitingId(member2.getId(), waiting.getId())
        ).isInstanceOf(DeletionNotAllowedException.class)
            .hasMessage("잘못된 삭제 요청입니다.");
    }

    @Test
    @DisplayName("예약 대기를 승인할 수 있다.")
    void approve() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Waiting waiting = waitingRepository.save(TestFixture.createWaiting(member, TEST_DATE, reservationTime, theme));

        //when & then
        assertThatCode(
                () -> waitingService.approve(waiting.getId())
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기를 Id로 삭제할 수 있다.")
    void deleteById() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Waiting waiting = waitingRepository.save(TestFixture.createWaiting(member, TEST_DATE, reservationTime, theme));

        //when & then
        assertThatCode(
                () -> waitingService.deleteById(waiting.getId())
        ).doesNotThrowAnyException();
    }
}