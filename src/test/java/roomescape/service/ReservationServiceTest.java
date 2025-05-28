package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.NotFoundReservationException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.exception.UnableReservationException;
import roomescape.persistence.MemberRepository;
import roomescape.persistence.ReservationRepository;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.persistence.ThemeRepository;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.MemberResult;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.service.result.ThemeResult;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class ReservationServiceTest {

    public static final LocalDate RESERVATION_DATE = LocalDate.now().plusDays(1);

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationService reservationService;


    @Test
    void 예약을_생성한다() {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme(null, "test", "description", "thumbnail");
        themeRepository.save(theme);

        Member member = new Member(null, "name", MemberRole.USER, "email", "password");
        memberRepository.save(member);

        CreateReservationParam createReservationParam = new CreateReservationParam(1L, RESERVATION_DATE, 1L, 1L,
                ReservationStatus.RESERVED);

        //when
        Long createdId = reservationService.create(createReservationParam, LocalDateTime.now());

        //then
        assertThat(reservationRepository.findById(createdId))
                .hasValue(new Reservation(
                        createdId,
                        member,
                        RESERVATION_DATE,
                        reservationTime,
                        theme,
                        ReservationStatus.RESERVED
                ));
    }

    @Test
    void 예약을_생성할때_timeId가_데이터베이스에_존재하지_않는다면_예외가_발생한다() {
        // given
        Theme theme = new Theme("test", "description", "thumbnail");
        Member member = new Member(null, "히스타", MemberRole.USER, "wtc@jjang.com", "password");
        themeRepository.save(theme);
        memberRepository.save(member);
        CreateReservationParam param = new CreateReservationParam(1L, RESERVATION_DATE, 1L, 1L,
                ReservationStatus.RESERVED);

        // when & then
        assertThatThrownBy(() -> reservationService.create(param, LocalDateTime.now()))
                .isInstanceOf(NotFoundReservationTimeException.class)
                .hasMessage("1에 해당하는 정보가 없습니다.");
    }

    @Test
    void id값으로_예약을_삭제할_수_있다() {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        Theme theme = new Theme(null, "test", "description", "thumbnail");
        Member member = new Member(null, "name", MemberRole.USER, "email", "password");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(new Reservation(
                null,
                member,
                RESERVATION_DATE,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));

        //when
        reservationService.deleteById(1L);

        //then
        assertThat(reservationRepository.findById(1L)).isEmpty();
    }

    @Test
    void 전체_예약을_조회할_수_있다() {
        //given
        Theme theme = new Theme(null, "test", "description", "thumbnail");
        ReservationTime reservationTime1 = new ReservationTime(null, LocalTime.of(12, 1));
        ReservationTime reservationTime2 = new ReservationTime(null, LocalTime.of(13, 1));
        Member member1 = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        Member member2 = new Member(null, "name2", MemberRole.USER, "email2", "password2");

        themeRepository.save(theme);
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        memberRepository.save(member1);
        memberRepository.save(member2);

        reservationRepository.save(new Reservation(
                null,
                member1,
                RESERVATION_DATE,
                reservationTime1,
                theme,
                ReservationStatus.RESERVED
        ));
        reservationRepository.save(new Reservation(
                null,
                member2,
                RESERVATION_DATE,
                reservationTime2,
                theme,
                ReservationStatus.RESERVED
        ));

        //when
        List<ReservationResult> reservations = reservationService.findAll();

        //then
        assertThat(reservations).hasSize(2);
    }

    @Test
    void id_로_예약을_찾을_수_있다() {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        Theme theme = new Theme(null, "test", "description", "thumbnail");
        Member member = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(new Reservation(
                null,
                member,
                RESERVATION_DATE,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));

        //when
        ReservationResult reservationResult = reservationService.findById(1L);

        //then
        assertThat(reservationResult).isEqualTo(
                new ReservationResult(
                        1L,
                        new MemberResult(1L, "name1", MemberRole.USER, "email1"),
                        RESERVATION_DATE,
                        new ReservationTimeResult(1L, LocalTime.of(12, 0)),
                        new ThemeResult(1L, "test", "description", "thumbnail"),
                        ReservationStatus.RESERVED
                )
        );
    }

    @Test
    void id에_해당하는_예약이_없는경우_예외가_발생한다() {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        Theme theme = new Theme(null, "test", "description", "thumbnail");
        Member member = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(new Reservation(
                null,
                member,
                RESERVATION_DATE,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));

        //when & then
        assertThatThrownBy(() -> reservationService.findById(2L))
                .isInstanceOf(NotFoundReservationException.class)
                .hasMessage("2에 해당하는 reservation 튜플이 없습니다.");
    }

    @Test
    void 날짜와_시간이_중복된_예약이_있으면_예외가_발생한다() {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        Theme theme = new Theme(null, "test", "description", "thumbnail");
        Member member = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(new Reservation(
                null,
                member,
                RESERVATION_DATE,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));

        CreateReservationParam param = new CreateReservationParam(1L, RESERVATION_DATE, 1L, 1L,
                ReservationStatus.RESERVED);

        //when & then
        assertThatThrownBy(() -> reservationService.create(param, LocalDateTime.now()))
                .isInstanceOf(UnableReservationException.class)
                .hasMessage("테마에 대해 날짜와 시간이 중복된 예약이 존재합니다.");
    }

    @ParameterizedTest
    @CsvSource({"2025-04-23T12:30, 2025-04-22T12:30", "2025-04-23T12:30, 2025-04-23T12:00"})
    void 지난_날짜에_대한_예약이라면_예외가_발생한다(LocalDateTime currentDateTime, LocalDateTime reservationDateTime) {
        //given
        themeRepository.save(new Theme("test", "description", "thumbnail"));
        reservationTimeRepository.save(new ReservationTime(null, reservationDateTime.toLocalTime()));
        memberRepository.save(new Member(null, "name1", MemberRole.USER, "email1", "password1"));
        CreateReservationParam param = new CreateReservationParam(1L, reservationDateTime.toLocalDate(), 1L, 1L,
                ReservationStatus.RESERVED);

        //when & then
        assertThatThrownBy(() -> reservationService.create(param, currentDateTime))
                .isInstanceOf(UnableReservationException.class)
                .hasMessage("지난 날짜와 시간에 대한 예약은 불가능합니다.");
    }

    @ParameterizedTest
    @CsvSource({"2025-04-23T12:30, 2025-04-23T12:30", "2025-04-23T12:30, 2025-04-23T12:39"})
    void 예약일이_오늘인_경우_예약_시간까지_10분도_남지_않았다면_예외가_발생한다(LocalDateTime currentDateTime, LocalDateTime reservationDateTime) {
        //given
        reservationTimeRepository.save(new ReservationTime(null, reservationDateTime.toLocalTime()));
        themeRepository.save(new Theme(null, "test", "description", "thumbnail"));
        memberRepository.save(new Member(null, "name1", MemberRole.USER, "email1", "password1"));
        CreateReservationParam param = new CreateReservationParam(1L, reservationDateTime.toLocalDate(), 1L, 1L,
                ReservationStatus.RESERVED);

        //when & then
        assertThatThrownBy(() -> reservationService.create(param, currentDateTime))
                .isInstanceOf(UnableReservationException.class)
                .hasMessage("예약 시간까지 10분도 남지 않아 예약이 불가합니다.");
    }

    @Test
    void 조건에_따라_예약을_조회할_수_있다_memberId() {
        //given
        Theme theme1 = new Theme(null, "test1", "description", "thumbnail");
        Theme theme2 = new Theme(null, "test2", "description", "thumbnail");
        ReservationTime reservationTime1 = new ReservationTime(null, LocalTime.of(12, 1));
        ReservationTime reservationTime2 = new ReservationTime(null, LocalTime.of(13, 1));
        Member member1 = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        Member member2 = new Member(null, "name2", MemberRole.USER, "email2", "password2");

        themeRepository.save(theme1);
        themeRepository.save(theme2);
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        memberRepository.save(member1);
        memberRepository.save(member2);

        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE, reservationTime1, theme1, ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE, reservationTime1, theme2, ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE, reservationTime2, theme1, ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE, reservationTime2, theme2, ReservationStatus.RESERVED));

        //when
        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(1L, null, null,
                null, ReservationStatus.RESERVED);

        //then
        assertThat(reservationResults.size()).isEqualTo(2);
    }

    @Test
    void 조건에_따라_예약을_조회할_수_있다_themeId() {
        //given
        Theme theme1 = new Theme(null, "test1", "description", "thumbnail");
        Theme theme2 = new Theme(null, "test2", "description", "thumbnail");
        ReservationTime reservationTime1 = new ReservationTime(null, LocalTime.of(12, 1));
        ReservationTime reservationTime2 = new ReservationTime(null, LocalTime.of(13, 1));
        Member member1 = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        Member member2 = new Member(null, "name2", MemberRole.USER, "email2", "password2");

        themeRepository.save(theme1);
        themeRepository.save(theme2);
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        memberRepository.save(member1);
        memberRepository.save(member2);

        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE, reservationTime1, theme1, ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE, reservationTime1, theme2, ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE, reservationTime2, theme1, ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE, reservationTime2, theme2, ReservationStatus.RESERVED));

        //when
        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(null, 1L, null,
                null, ReservationStatus.RESERVED);

        //then
        assertThat(reservationResults.size()).isEqualTo(2);
    }

    @Test
    void 조건에_따라_예약을_조회할_수_있다_dateTo() {
        //given
        Theme theme1 = new Theme(null, "test1", "description", "thumbnail");
        Theme theme2 = new Theme(null, "test2", "description", "thumbnail");
        ReservationTime reservationTime1 = new ReservationTime(null, LocalTime.of(12, 1));
        ReservationTime reservationTime2 = new ReservationTime(null, LocalTime.of(13, 1));
        Member member1 = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        Member member2 = new Member(null, "name2", MemberRole.USER, "email2", "password2");

        themeRepository.save(theme1);
        themeRepository.save(theme2);
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        memberRepository.save(member1);
        memberRepository.save(member2);

        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE.minusDays(1), reservationTime1, theme1,
                        ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE.minusDays(1), reservationTime1, theme2,
                        ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE, reservationTime2, theme1, ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE, reservationTime2, theme2, ReservationStatus.RESERVED));

        // when
        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(null, null,
                RESERVATION_DATE, null, ReservationStatus.RESERVED);

        // then
        assertThat(reservationResults.size()).isEqualTo(2);
    }

    @Test
    void 조건에_따라_예약을_조회할_수_있다_dateFrom() {
        //given
        Theme theme1 = new Theme(null, "test1", "description", "thumbnail");
        Theme theme2 = new Theme(null, "test2", "description", "thumbnail");
        ReservationTime reservationTime1 = new ReservationTime(null, LocalTime.of(12, 1));
        ReservationTime reservationTime2 = new ReservationTime(null, LocalTime.of(13, 1));
        Member member1 = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        Member member2 = new Member(null, "name2", MemberRole.USER, "email2", "password2");

        themeRepository.save(theme1);
        themeRepository.save(theme2);
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        memberRepository.save(member1);
        memberRepository.save(member2);

        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE.plusDays(1), reservationTime1, theme1,
                        ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE.plusDays(1), reservationTime1, theme2,
                        ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE.plusDays(2), reservationTime2, theme1,
                        ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE.plusDays(2), reservationTime2, theme2,
                        ReservationStatus.RESERVED));

        // when
        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(null, null,
                null, RESERVATION_DATE.plusDays(1), ReservationStatus.RESERVED);

        // then
        assertThat(reservationResults.size()).isEqualTo(2);
    }

    @Test
    void 조건에_따라_예약을_조회할_수_있다_status() {
        //given
        Theme theme1 = new Theme(null, "test1", "description", "thumbnail");
        Theme theme2 = new Theme(null, "test2", "description", "thumbnail");
        ReservationTime reservationTime1 = new ReservationTime(null, LocalTime.of(12, 1));
        ReservationTime reservationTime2 = new ReservationTime(null, LocalTime.of(13, 1));
        Member member1 = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        Member member2 = new Member(null, "name2", MemberRole.USER, "email2", "password2");

        themeRepository.save(theme1);
        themeRepository.save(theme2);
        reservationTimeRepository.save(reservationTime1);
        reservationTimeRepository.save(reservationTime2);
        memberRepository.save(member1);
        memberRepository.save(member2);

        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE.minusDays(1), reservationTime1, theme1,
                        ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE.minusDays(1), reservationTime1, theme2,
                        ReservationStatus.CANCELED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE, reservationTime2, theme1, ReservationStatus.RESERVED));
        reservationRepository.save(
                new Reservation(null, member2, RESERVATION_DATE, reservationTime2, theme2, ReservationStatus.CANCELED));

        // when
        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(null, null,
                null, null, ReservationStatus.CANCELED);

        // then
        assertThat(reservationResults.size()).isEqualTo(2);
    }

    @Test
    void 조건이_없으면_모든_예약을_반환한다() {
        //given
        Theme theme1 = new Theme(null, "test1", "description", "thumbnail");
        ReservationTime reservationTime1 = new ReservationTime(null, LocalTime.of(12, 1));
        Member member1 = new Member(null, "name1", MemberRole.USER, "email1", "password1");

        themeRepository.save(theme1);
        reservationTimeRepository.save(reservationTime1);
        memberRepository.save(member1);

        reservationRepository.save(
                new Reservation(null, member1, RESERVATION_DATE, reservationTime1, theme1, ReservationStatus.RESERVED));

        //when
        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(1L, null,
                RESERVATION_DATE, null, ReservationStatus.RESERVED);

        //then
        assertThat(reservationResults.size()).isEqualTo(1);
    }

    @Test
    void 예약_대기를_승인할때_이미_다른_유저의_예약이_있다면_예외가_발생한다() {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        Theme theme = new Theme(null, "test", "description", "thumbnail");
        Member alreadyReservedMember = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        Member willReserveMember = new Member(null, "name2", MemberRole.USER, "email2", "password2");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(alreadyReservedMember);
        memberRepository.save(willReserveMember);
        reservationRepository.save(new Reservation(
                null,
                alreadyReservedMember,
                RESERVATION_DATE,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));
        reservationRepository.save(new Reservation(
                null,
                willReserveMember,
                RESERVATION_DATE,
                reservationTime,
                theme,
                ReservationStatus.WAITING
        ));

        //when & then
        assertThatThrownBy(() -> reservationService.approveWaitingReservation(2L))
                .isInstanceOf(UnableReservationException.class)
                .hasMessage("이미 다른 유저의 예약이 존재해서, 예약을 승인할 수 없습니다.");
    }

    @Test
    void 예약_대기를_승인할때_예약이_대기중이_아니라면_예외가_발생한다() {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        Theme theme = new Theme(null, "test", "description", "thumbnail");
        Member member = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(new Reservation(
                null,
                member,
                RESERVATION_DATE,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));

        //when & then
        assertThatThrownBy(() -> reservationService.approveWaitingReservation(1L))
                .isInstanceOf(UnableReservationException.class)
                .hasMessage("대기 중인 예약만 승인할 수 있습니다.");
    }

    @Test
    void 예약_대기를_승인할때_예약이_존재하지_않는다면_예외가_발생한다() {
        //when & then
        assertThatThrownBy(() -> reservationService.approveWaitingReservation(1L))
                .isInstanceOf(NotFoundReservationException.class)
                .hasMessage("1에 해당하는 reservation 튜플이 없습니다.");
    }

    @Test
    void 예약_대기를_승인할때_다른_유저의_예약이_없다면_승인된다() {
        //given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(12, 0));
        Theme theme = new Theme(null, "test", "description", "thumbnail");
        Member member = new Member(null, "name1", MemberRole.USER, "email1", "password1");
        reservationTimeRepository.save(reservationTime);
        themeRepository.save(theme);
        memberRepository.save(member);
        reservationRepository.save(new Reservation(
                null,
                member,
                RESERVATION_DATE,
                reservationTime,
                theme,
                ReservationStatus.WAITING
        ));

        //when
        reservationService.approveWaitingReservation(1L);

        //then
        assertThat(reservationRepository.findById(1L).get().getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }
}
