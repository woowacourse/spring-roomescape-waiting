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
import roomescape.exception.UnableCreateReservationException;
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

        CreateReservationParam createReservationParam = new CreateReservationParam(1L, RESERVATION_DATE, 1L, 1L);

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

    // TODO: 테스트 코드 리팩토링

//    @Test
//    void 예약을_생성할때_timeId가_데이터베이스에_존재하지_않는다면_예외가_발생한다() {
//        //give
//        Theme theme = new Theme(1L, "test", "description", "thumbnail");
//        themeRepository.create(theme);
//        CreateMemberQuery member = new CreateMemberQuery("name", MemberRole.USER, "email", "password");
//        memberRepository.create(member);
//        CreateReservationParam createReservationParam = new CreateReservationParam(1L, RESERVATION_DATE, 1L, 1L);
//
//        //when & then
//        assertThatThrownBy(() -> reservationService.create(createReservationParam, LocalDateTime.now()))
//                .isInstanceOf(NotFoundReservationTimeException.class)
//                .hasMessage("1에 해당하는 정보가 없습니다.");
//    }
//
//    @Test
//    void id값으로_예약을_삭제할_수_있다() {
//        //given
//        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
//        Theme theme = new Theme(1L, "test", "description", "thumbnail");
//        Member member = new Member(1L, "name", MemberRole.USER, "email", "password");
//        reservationRepository.create(new CreateReservationQuery(member, RESERVATION_DATE, reservationTime, theme));
//
//        //when
//        reservationService.deleteById(1L);
//
//        //then
//        assertThat(reservationRepository.findById(1L)).isEmpty();
//    }
//
//    @Test
//    void 전체_예약을_조회할_수_있다() {
//        //given
//        Theme theme = new Theme(1L, "test", "description", "thumbnail");
//        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(12, 1));
//        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(13, 1));
//        Member member1 = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        Member member2 = new Member(2L, "name2", MemberRole.USER, "email2", "password2");
//
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE, reservationTime2, theme));
//
//        //when
//        List<ReservationResult> reservationResults = reservationService.findAll();
//
//        //then
//        assertThat(reservationResults).isEqualTo(List.of(
//                new ReservationResult(1L, new MemberResult(1L, "name1", MemberRole.USER, "email1"), RESERVATION_DATE,
//                        new ReservationTimeResult(1L, LocalTime.of(12, 1)),
//                        new ThemeResult(1L, "test", "description", "thumbnail")),
//                new ReservationResult(2L, new MemberResult(2L, "name2", MemberRole.USER, "email2"), RESERVATION_DATE,
//                        new ReservationTimeResult(2L, LocalTime.of(13, 1)),
//                        new ThemeResult(1L, "test", "description", "thumbnail"))
//        ));
//    }
//
//    @Test
//    void id_로_예약을_찾을_수_있다() {
//        //given
//        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
//        Theme theme = new Theme(1L, "test", "description", "thumbnail");
//        Member member = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        reservationRepository.create(new CreateReservationQuery(member, RESERVATION_DATE, reservationTime, theme));
//
//        //when
//        ReservationResult reservationResult = reservationService.findById(1L);
//
//        //then
//        assertThat(reservationResult).isEqualTo(
//                new ReservationResult(1L, new MemberResult(1L, "name1", MemberRole.USER, "email1"),
//                        RESERVATION_DATE,
//                        new ReservationTimeResult(1L, LocalTime.of(12, 0)),
//                        new ThemeResult(1L, "test", "description", "thumbnail"))
//        );
//    }
//
//    @Test
//    void id에_해당하는_예약이_없는경우_예외가_발생한다() {
//        //given
//        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
//        Theme theme = new Theme(1L, "test", "description", "thumbnail");
//        Member member = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        reservationRepository.create(new CreateReservationQuery(member, RESERVATION_DATE, reservationTime, theme));
//
//        //when & then
//        assertThatThrownBy(() -> reservationService.findById(2L))
//                .isInstanceOf(NotFoundReservationException.class)
//                .hasMessage("2에 해당하는 reservation 튜플이 없습니다.");
//    }
//
//    @Test
//    void 날짜와_시간이_중복된_예약이_있으면_예외가_발생한다() {
//        //given
//        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(12, 0));
//        Theme theme = new Theme(1L, "test", "description", "thumbnail");
//        Member member = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        reservationTimeRepository.create(reservationTime);
//        themeRepository.create(theme);
//        memberRepository.create(new CreateMemberQuery(member.getName(), MemberRole.USER, member.getEmail(), member.getPassword()));
//        reservationRepository.create(new CreateReservationQuery(member, RESERVATION_DATE, reservationTime, theme));
//
//        //when & then
//        assertThatThrownBy(() -> reservationService.create(new CreateReservationParam(1L, RESERVATION_DATE, 1L, 1L), LocalDateTime.now()))
//                .isInstanceOf(UnableCreateReservationException.class)
//                .hasMessage("테마에 대해 날짜와 시간이 중복된 예약이 존재합니다.");
//    }
//
//    @ParameterizedTest
//    @CsvSource({"2025-04-23T12:30, 2025-04-22T12:30", "2025-04-23T12:30, 2025-04-23T12:00"})
//    void 지난_날짜에_대한_예약이라면_예외가_발생한다(LocalDateTime currentDateTime, LocalDateTime reservationDateTime) {
//        //given
//        themeRepository.create(new Theme(1L, "test", "description", "thumbnail"));
//        reservationTimeRepository.create(new ReservationTime(1L, reservationDateTime.toLocalTime()));
//        memberRepository.create(new CreateMemberQuery("name1", MemberRole.USER, "email1", "password1"));
//
//        //when & then
//        assertThatThrownBy(() -> reservationService.create(new CreateReservationParam(1L, reservationDateTime.toLocalDate(), 1L, 1L), currentDateTime))
//                .isInstanceOf(UnableCreateReservationException.class)
//                .hasMessage("지난 날짜와 시간에 대한 예약은 불가능합니다.");
//    }
//
//    @ParameterizedTest
//    @CsvSource({"2025-04-23T12:30, 2025-04-23T12:30", "2025-04-23T12:30, 2025-04-23T12:39"})
//    void 예약일이_오늘인_경우_예약_시간까지_10분도_남지_않았다면_예외가_발생한다(LocalDateTime currentDateTime, LocalDateTime reservationDateTime) {
//        //given
//        reservationTimeRepository.create(new ReservationTime(1L, reservationDateTime.toLocalTime()));
//        themeRepository.create(new Theme(1L, "test", "description", "thumbnail"));
//        memberRepository.create(new CreateMemberQuery("name1", MemberRole.USER, "email1", "password1"));
//
//        //when & then
//        assertThatThrownBy(() -> reservationService.create(new CreateReservationParam(1L, reservationDateTime.toLocalDate(), 1L, 1L), currentDateTime))
//                .isInstanceOf(UnableCreateReservationException.class)
//                .hasMessage("예약 시간까지 10분도 남지 않아 예약이 불가합니다.");
//    }
//
//    @Test
//    void 조건에_따라_예약을_조회할_수_있다_memberId() {
//        //given
//        Theme theme1 = new Theme(1L, "test", "description", "thumbnail");
//        Theme theme2 = new Theme(2L, "test", "description", "thumbnail");
//        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(12, 1));
//        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(13, 1));
//        Member member1 = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        Member member2 = new Member(2L, "name2", MemberRole.USER, "email2", "password2");
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme1));
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme2));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE, reservationTime2, theme1));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE, reservationTime2, theme2));
//
//        //when
//        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(1L, null, null, null);
//
//        //then
//        assertThat(reservationResults.size()).isEqualTo(2);
//    }
//
//    @Test
//    void 조건에_따라_예약을_조회할_수_있다_themeId() {
//        //given
//        Theme theme1 = new Theme(1L, "test", "description", "thumbnail");
//        Theme theme2 = new Theme(2L, "test", "description", "thumbnail");
//        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(12, 1));
//        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(13, 1));
//        Member member1 = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        Member member2 = new Member(2L, "name2", MemberRole.USER, "email2", "password2");
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme1));
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme2));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE, reservationTime2, theme1));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE, reservationTime2, theme2));
//
//        //when
//        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(null, 2L, null, null);
//
//        //then
//        assertThat(reservationResults.size()).isEqualTo(2);
//    }
//
//    @Test
//    void 조건에_따라_예약을_조회할_수_있다_dateFrom() {
//        //given
//        Theme theme1 = new Theme(1L, "test", "description", "thumbnail");
//        Theme theme2 = new Theme(2L, "test", "description", "thumbnail");
//        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(12, 1));
//        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(13, 1));
//        Member member1 = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        Member member2 = new Member(2L, "name2", MemberRole.USER, "email2", "password2");
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme1));
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme2));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE.plusDays(1), reservationTime2, theme1));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE.plusDays(1), reservationTime2, theme2));
//
//        //when
//        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(null, null, RESERVATION_DATE, null);
//
//        //then
//        assertThat(reservationResults.size()).isEqualTo(4);
//    }
//
//    @Test
//    void 조건에_따라_예약을_조회할_수_있다_dateTo() {
//        //given
//        Theme theme1 = new Theme(1L, "test", "description", "thumbnail");
//        Theme theme2 = new Theme(2L, "test", "description", "thumbnail");
//        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(12, 1));
//        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(13, 1));
//        Member member1 = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        Member member2 = new Member(2L, "name2", MemberRole.USER, "email2", "password2");
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme1));
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme2));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE.plusDays(1), reservationTime2, theme1));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE.plusDays(1), reservationTime2, theme2));
//
//        //when
//        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(null, null, null, RESERVATION_DATE);
//
//        //then
//        assertThat(reservationResults.size()).isEqualTo(2);
//    }
//
//    @Test
//    void 조건이_없으면_전체_예약을_반환한다() {
//        //given
//        Theme theme1 = new Theme(1L, "test", "description", "thumbnail");
//        Theme theme2 = new Theme(2L, "test", "description", "thumbnail");
//        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(12, 1));
//        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(13, 1));
//        Member member1 = new Member(1L, "name1", MemberRole.USER, "email1", "password1");
//        Member member2 = new Member(2L, "name2", MemberRole.USER, "email2", "password2");
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme1));
//        reservationRepository.create(new CreateReservationQuery(member1, RESERVATION_DATE, reservationTime1, theme2));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE.plusDays(1), reservationTime2, theme1));
//        reservationRepository.create(new CreateReservationQuery(member2, RESERVATION_DATE.plusDays(1), reservationTime2, theme2));
//
//        //when
//        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(null, null, null, null);
//
//        //then
//        assertThat(reservationResults.size()).isEqualTo(4);
//    }
}
