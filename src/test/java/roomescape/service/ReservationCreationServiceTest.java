package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.DEFAULT_DATE;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnAvailableReservationException;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationCreationServiceTest {

    @Autowired
    private ReservationCreationService reservationCreationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    public static final LocalDate RESERVATION_DATE = LocalDate.now().plusDays(1);

    @Test
    void 예약을_생성한다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        CreateReservationParam createReservationParam = new CreateReservationParam(member.getId(), RESERVATION_DATE, reservationTime.getId(), theme.getId());

        //when
        ReservationResult reservationResult = reservationCreationService.create(createReservationParam);

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
        assertThatThrownBy(() -> reservationCreationService.create(createReservationParam))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 날짜와_시간이_중복된_예약이_있으면_예외가_발생한다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = reservationRepository.save(TestFixture.createDefaultReservation(member, DEFAULT_DATE, reservationTime, theme));

        //when & then
        assertThatThrownBy(() -> reservationCreationService.create(new CreateReservationParam(member.getId(), reservation.getDate(), reservationTime.getId(), theme.getId())))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("테마에 대해 날짜와 시간이 중복된 예약이 존재합니다.");
    }

    @Test
    void 지난_날짜에_대한_예약이라면_예외가_발생한다() {
        //given
        LocalDateTime reservationDateTime = LocalDateTime.now().minusDays(1);

        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTimeByTime(reservationDateTime.toLocalTime()));
        Member member = memberRepository.save(TestFixture.createDefaultMember());

        //when & then
        assertThatThrownBy(() -> reservationCreationService.create(new CreateReservationParam(member.getId(), reservationDateTime.toLocalDate(), reservationTime.getId(), theme.getId())))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("지난 날짜와 시간에 대한 예약은 불가능합니다.");
    }

    @Test
    void 예약일이_오늘인_경우_예약_시간까지_10분도_남지_않았다면_예외가_발생한다() {
        //given
        LocalDateTime reservationDateTime = LocalDateTime.now().plusMinutes(10);

        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTimeByTime(reservationDateTime.toLocalTime()));
        Member member = memberRepository.save(TestFixture.createDefaultMember());

        //when & then
        assertThatThrownBy(() -> reservationCreationService.create(new CreateReservationParam(member.getId(), reservationDateTime.toLocalDate(), reservationTime.getId(), theme.getId())))
                .isInstanceOf(UnAvailableReservationException.class)
                .hasMessage("예약 시간까지 10분도 남지 않아 예약이 불가합니다.");
    }

}
