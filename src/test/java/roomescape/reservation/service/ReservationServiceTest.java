package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.constant.TestData.RESERVATION_COUNT;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.auth.dto.LoginMember;
import roomescape.exception.NotFoundException;
import roomescape.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
@Sql("/data.sql")
class ReservationServiceTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository timeRepo;

    @Autowired
    private ThemeRepository themeRepo;

    @Autowired
    private MemberRepository memberRepo;

    private Clock clock = Clock.systemDefaultZone();

    private ReservationService service;

    private ReservationTime time1;

    private Theme theme1;

    private Member member;

    private Reservation r1;

    @BeforeEach
    void setUp() {
        service = new ReservationService(clock, reservationRepository, timeRepo, themeRepo, memberRepo);
        time1 = ReservationTime.from(LocalTime.of(14, 0));

        theme1 = Theme.of("테마1", "설명1", "썸네일1");

        member = Member.withDefaultRole("member", "mem@naver.com", "1234");

        r1 = Reservation.booked(LocalDate.of(2999, 5, 11), time1, theme1, member, LocalDateTime.now(clock));
    }

    @Test
    void 모든_예약을_조회한다() {
        // when
        List<ReservationResponse> responses = service.findReservationsByCriteria(
                new ReservationSearchRequest(null, null, null, null));

        // then
        assertThat(responses).hasSize(RESERVATION_COUNT);
    }

    @Test
    void 중복된_날짜와_시간이면_예외가_발생한다() {
        // given: r1과 동일한 date/time 요청
        timeRepo.save(time1);
        themeRepo.save(theme1);
        memberRepo.save(member);
        reservationRepository.save(r1);
        ReservationRequest dupReq = new ReservationRequest(r1.getDate(), time1.getId(), theme1.getId());
        final LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());
        // when
        // then
        assertThatThrownBy(() -> service.saveReservation(dupReq, loginMember))
                .isInstanceOf(ReservationException.class)
                .hasMessage("해당 시간은 이미 예약되어있습니다.");
    }

    @Test
    void 지나간_날짜와_시간이면_예외가_발생한다() {
        // given: r1과 동일한 date/time 요청
        timeRepo.save(time1);
        themeRepo.save(theme1);
        memberRepo.save(member);
        ReservationRequest request = new ReservationRequest(LocalDate.of(2000, 10, 8), time1.getId(), theme1.getId());
        final LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());

        // when
        // then
        assertThatThrownBy(() -> service.saveReservation(request, loginMember))
                .isInstanceOf(ReservationException.class)
                .hasMessage("예약은 현재 시간 이후로 가능합니다.");
    }

    @Test
    void 새로운_예약은_정상_생성된다() {
        // given
        timeRepo.save(time1);
        themeRepo.save(theme1);
        memberRepo.save(member);
        final LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());

        ReservationRequest req = new ReservationRequest(LocalDate.of(2999, 4, 21), time1.getId(), theme1.getId());

        // when
        ReservationResponse result = service.saveReservation(req, loginMember);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(result.id()).isEqualTo(RESERVATION_COUNT + 1L);
            soft.assertThat(result.date()).isEqualTo(LocalDate.of(2999, 4, 21));
            soft.assertThat(result.time())
                    .satisfies(rt -> {
                        assertThat(rt.id()).isEqualTo(time1.getId());
                        assertThat(rt.startAt()).isEqualTo(time1.getStartAt());
                    });
        });
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(reservationRepository.findByCriteria(null, null, null, null))
                    .hasSize(RESERVATION_COUNT + 1);
            soft.assertThat(
                            reservationRepository.findByCriteria(null, null, null, null).get(RESERVATION_COUNT).getTime()
                                    .getStartAt())
                    .isEqualTo(time1.getStartAt());
        });
    }

    @Test
    void 예약을_삭제한다() {
        // when
        memberRepo.save(member);
        timeRepo.save(time1);
        themeRepo.save(theme1);
        reservationRepository.save(r1);
        service.deleteReservation(r1.getId());

        // then
        assertThat(reservationRepository.findByCriteria(null, null, null, null))
                .hasSize(RESERVATION_COUNT)
                .extracting(Reservation::getId)
                .doesNotContain(r1.getId());
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        // when
        // then
        assertThatThrownBy(() -> service.deleteReservation(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다. id=999");
    }

    @Test
    void 내_모든_예약을_조회한다() {
        // given
        memberRepo.save(member);
        timeRepo.save(time1);
        themeRepo.save(theme1);
        reservationRepository.save(Reservation.booked(LocalDate.of(2999,5,1), time1, theme1, member,
                LocalDateTime.now(clock)));
        reservationRepository.save(Reservation.booked(LocalDate.of(2999,5,2), time1, theme1, member,
                LocalDateTime.now(clock)));
        reservationRepository.save(Reservation.booked(LocalDate.of(2999,5,3), time1, theme1, member,
                LocalDateTime.now(clock)));
        reservationRepository.save(Reservation.booked(LocalDate.of(2999,5,4), time1, theme1, member,
                LocalDateTime.now(clock)));
        final LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());

        // when
        List<MyReservationResponse> myReservations = service.findMyReservations(loginMember);

        // then
        assertThat(myReservations).hasSize(4);
    }
}
