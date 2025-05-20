package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.constant.TestData.RESERVATION_COUNT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.NotFoundException;
import roomescape.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.Password;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@DataJpaTest
@Sql("/data.sql")
@Import(ReservationService.class)
class ReservationServiceTest {

    @Autowired
    private TestEntityManager tm;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationService service;

    private ReservationTime time;

    private Theme theme;

    private Member member;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        time = ReservationTime.from(LocalTime.of(14, 0));

        theme = Theme.of("테마1", "설명1", "썸네일1");

        member = Member.builder()
                .name("김철수")
                .email("member@example.com")
                .password(Password.createForMember("pass123"))
                .role(MemberRole.MEMBER)
                .build();

        reservation = Reservation.booked(LocalDate.of(2999, 5, 11), time, theme, member);
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
        tm.persistAndFlush(time);
        tm.persistAndFlush(theme);
        tm.persistAndFlush(member);
        tm.persistAndFlush(reservation);
        ReservationRequest dupReq = new ReservationRequest(reservation.getDate(), time.getId(), theme.getId());
        final LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());
        tm.clear();
        // when
        // then
        assertThatThrownBy(() -> service.saveReservation(dupReq, loginMember))
                .isInstanceOf(ReservationException.class)
                .hasMessage("해당 시간은 이미 예약되어있습니다.");
    }

    @Test
    void 지나간_날짜와_시간이면_예외가_발생한다() {
        // given: r1과 동일한 date/time 요청
        tm.persistAndFlush(time);
        tm.persistAndFlush(theme);
        tm.persistAndFlush(member);
        ReservationRequest request = new ReservationRequest(LocalDate.of(2000, 10, 8), time.getId(), theme.getId());
        final LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());
        tm.clear();

        // when
        // then
        assertThatThrownBy(() -> service.saveReservation(request, loginMember))
                .isInstanceOf(ReservationException.class)
                .hasMessage("예약은 현재 시간 이후로 가능합니다.");
    }

    @Test
    void 새로운_예약은_정상_생성된다() {
        // given
        tm.persistAndFlush(time);
        tm.persistAndFlush(theme);
        tm.persistAndFlush(member);
        final LoginMember loginMember = new LoginMember(member.getId(), member.getName(), member.getEmail(),
                member.getRole());

        ReservationRequest req = new ReservationRequest(LocalDate.of(2999, 4, 21), time.getId(), theme.getId());

        // when
        ReservationResponse result = service.saveReservation(req, loginMember);

        // then
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(result.id()).isEqualTo(RESERVATION_COUNT + 1L);
            soft.assertThat(result.date()).isEqualTo(LocalDate.of(2999, 4, 21));
            soft.assertThat(result.time())
                    .satisfies(rt -> {
                        assertThat(rt.id()).isEqualTo(time.getId());
                        assertThat(rt.startAt()).isEqualTo(time.getStartAt());
                    });
        });
    }

    @Test
    void 예약을_삭제한다() {
        // given
        tm.persistAndFlush(member);
        tm.persistAndFlush(time);
        tm.persistAndFlush(theme);
        tm.persistAndFlush(reservation);

        // when
        service.deleteReservation(reservation.getId());

        // then
        assertThat(reservationRepository.findByCriteria(null, null, null, null))
                .hasSize(RESERVATION_COUNT)
                .extracting(Reservation::getId)
                .doesNotContain(reservation.getId());
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_예외가_발생한다() {
        // when
        // then
        assertThatThrownBy(() -> service.deleteReservation(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 예약입니다. id=999");
    }
}
