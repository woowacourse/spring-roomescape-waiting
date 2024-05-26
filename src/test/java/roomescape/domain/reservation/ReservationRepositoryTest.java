package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.Fixture.DATE_1;
import static roomescape.fixture.Fixture.MEMBER_1;
import static roomescape.fixture.Fixture.RESERVATION_TIME_1;
import static roomescape.fixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 아이디, 테마 아이디, 시작 날짜, 종료 날짜로 예약들을 조회한다.")
    void findAllByConditions() {
        // given
        LocalDate date = LocalDate.of(2024, 5, 5);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_1);
        Theme theme = themeRepository.save(THEME_1);
        Member member = memberRepository.save(MEMBER_1);
        ReservationDetail detail = new ReservationDetail(date, reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(new Reservation(detail, member));

        // when
        List<Reservation> reservations = reservationRepository.findAllByConditions(
                member.getId(),
                theme.getId(),
                LocalDate.of(2024, 5, 5),
                LocalDate.of(2024, 5, 5)
        );

        // then
        assertThat(reservations).hasSize(1).containsExactly(savedReservation);
    }

    @Test
    @DisplayName("아아디로 예약을 조회한다.")
    void getById() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_1);
        Theme theme = themeRepository.save(THEME_1);
        Member member = memberRepository.save(MEMBER_1);
        ReservationDetail detail = new ReservationDetail(DATE_1, reservationTime, theme);

        Reservation savedReservation = reservationRepository.save(new Reservation(detail, member));

        // when
        Reservation reservation = reservationRepository.getById(savedReservation.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservation.getId()).isEqualTo(savedReservation.getId());
            softly.assertThat(reservation.getDetail()).isEqualTo(detail);
            softly.assertThat(reservation.getMember()).isEqualTo(member);
        });
    }

    @Test
    @DisplayName("아이디로 예약을 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdWhenNotExist() {
        assertThatThrownBy(() -> reservationRepository.getById(-1L))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage(String.format("해당 id의 예약이 존재하지 않습니다. (id: %d)", -1L));
    }
}
