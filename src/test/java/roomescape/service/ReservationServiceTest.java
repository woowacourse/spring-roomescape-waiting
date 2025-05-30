package roomescape.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.*;
import roomescape.exception.NotFoundReservationException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.exception.UnAvailableReservationException;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.TEST_DATE;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceTest {

    public static final LocalDate RESERVATION_DATE = LocalDate.now().plusDays(1);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    void 예약을_생성한다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        CreateReservationParam createReservationParam = new CreateReservationParam(member.getId(), RESERVATION_DATE, reservationTime.getId(), theme.getId());

        //when
        ReservationResult reservationResult = reservationService.create(createReservationParam, LocalDateTime.now());

        //then
        Reservation reservation = reservationRepository.findById(reservationResult.id()).get();
        assertThat(reservation.getId()).isNotNull();
    }

    @Test
    void 예약을_생성할때_timeId가_데이터베이스에_존재하지_않는다면_예외가_발생한다() {
        //give
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        CreateReservationParam createReservationParam = new CreateReservationParam(member.getId(), RESERVATION_DATE, 1L, theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.create(createReservationParam, LocalDateTime.now()))
                .isInstanceOf(NotFoundReservationTimeException.class)
                .hasMessage("1에 해당하는 시간 정보가 없습니다.");
    }

    @Test
    void id값으로_예약을_삭제할_수_있다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = reservationRepository.save(TestFixture.createDefaultReservation(member, TEST_DATE, reservationTime, theme));
        Waiting waiting = waitingRepository.save(TestFixture.createWaiting(member, TEST_DATE, reservationTime, theme));

        //when
        reservationService.deleteByIdAndApproveFirstWaiting(reservation.getId());

        //then
        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

    @Test
    void 전체_예약을_조회할_수_있다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation1 = reservationRepository.save(TestFixture.createDefaultReservation(member, TEST_DATE, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(TestFixture.createDefaultReservation(member, TEST_DATE.plusDays(1), reservationTime, theme));

        //when
        List<ReservationResult> reservationResults = reservationService.findAll();

        //then
        assertAll(
                () -> assertThat(reservationResults).hasSize(2),
                () -> assertThat(reservationResults.getFirst())
                        .isEqualTo(ReservationResult.from(reservation1))
        );
    }

    @Test
    void id_로_예약을_찾을_수_있다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = reservationRepository.save(TestFixture.createDefaultReservation(member, TEST_DATE, reservationTime, theme));

        //when
        ReservationResult reservationResult = reservationService.findById(reservation.getId());

        //then
        assertThat(reservationResult).isEqualTo(ReservationResult.from(reservation));
    }

    @Test
    void id에_해당하는_예약이_없는경우_예외가_발생한다() {
        Long noId = 99L;

        assertThatThrownBy(() -> reservationService.findById(noId))
                .isInstanceOf(NotFoundReservationException.class)
                .hasMessage(noId + "에 해당하는 reservation 튜플이 없습니다.");
    }

    @Test
    void 날짜와_시간이_중복된_예약이_있으면_예외가_발생한다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = reservationRepository.save(TestFixture.createDefaultReservation(member, TEST_DATE, reservationTime, theme));

        //when & then
        assertThatThrownBy(() -> reservationService.create(new CreateReservationParam(member.getId(), reservation.getDate(), reservationTime.getId(), theme.getId()), LocalDateTime.now()))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("테마에 대해 날짜와 시간이 중복된 예약이 존재합니다.");
    }

    @ParameterizedTest
    @CsvSource({"2025-04-23T12:30, 2025-04-22T12:30",
            "2025-04-23T12:30, 2025-04-23T12:00"})
    void 지난_날짜에_대한_예약이라면_예외가_발생한다(LocalDateTime currentDateTime, LocalDateTime reservationDateTime) {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTimeByTime(reservationDateTime.toLocalTime()));
        Member member = memberRepository.save(TestFixture.createDefaultMember());

        //when & then
        assertThatThrownBy(() -> reservationService.create(new CreateReservationParam(member.getId(), reservationDateTime.toLocalDate(), reservationTime.getId(), theme.getId()), currentDateTime))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("지난 날짜와 시간에 대한 예약은 불가능합니다.");
    }

    @ParameterizedTest
    @CsvSource({"2025-04-23T12:30, 2025-04-23T12:30", "2025-04-23T12:30, 2025-04-23T12:39"})
    void 예약일이_오늘인_경우_예약_시간까지_10분도_남지_않았다면_예외가_발생한다(LocalDateTime currentDateTime, LocalDateTime reservationDateTime) {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTimeByTime(reservationDateTime.toLocalTime()));
        Member member = memberRepository.save(TestFixture.createDefaultMember());

        //when & then
        assertThatThrownBy(() -> reservationService.create(new CreateReservationParam(member.getId(), reservationDateTime.toLocalDate(), reservationTime.getId(), theme.getId()), currentDateTime))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("예약 시간까지 10분도 남지 않아 예약이 불가합니다.");
    }
}