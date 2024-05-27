package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DBTest;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;

class ReservationSearchSpecificationTest extends DBTest {

    private Reservation saved;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(TestFixture.getMember1());
        ReservationTime time = timeRepository.save(TestFixture.getReservationTime10AM());
        Theme theme = themeRepository.save(TestFixture.getTheme1());
        saved = reservationRepository.save(
                new Reservation(member, TestFixture.TOMORROW, time, theme, Status.CONFIRMED));
    }

    @DisplayName("특정 테마에 대한 예약을 찾는다.")
    @Test
    void sameThemeId() {
        // given
        ReservationSearchSpecification spec = new ReservationSearchSpecification().sameThemeId(
                saved.getTheme().getId());

        // when
        List<Reservation> found = reservationRepository.findAll(spec.build());

        // then
        assertThat(found).containsExactly(saved);
    }

    @DisplayName("특정 회원에 대한 예약을 찾는다.")
    @Test
    void sameMemberId() {
        // given
        ReservationSearchSpecification spec = new ReservationSearchSpecification().sameMemberId(
                saved.getMember().getId());

        // when
        List<Reservation> found = reservationRepository.findAll(spec.build());

        // then
        assertThat(found).containsExactly(saved);
    }

    @DisplayName("특정 시간에 대한 예약을 찾는다.")
    @Test
    void sameTimeId() {
        // given
        ReservationSearchSpecification spec = new ReservationSearchSpecification().sameTimeId(
                saved.getTime().getId());

        // when
        List<Reservation> found = reservationRepository.findAll(spec.build());

        // then
        assertThat(found).containsExactly(saved);
    }

    @DisplayName("특정 날짜에 대한 예약을 찾는다.")
    @Test
    void sameDate() {
        // given
        ReservationSearchSpecification spec = new ReservationSearchSpecification().sameDate(
                saved.getDate());

        // when
        List<Reservation> found = reservationRepository.findAll(spec.build());

        // then
        assertThat(found).containsExactly(saved);
    }

    @DisplayName("특정 예약 상태에 대한 예약을 찾는다.")
    @Test
    void sameStatus() {
        // given
        ReservationSearchSpecification spec = new ReservationSearchSpecification().sameStatus(
                saved.getStatus());

        // when
        List<Reservation> found = reservationRepository.findAll(spec.build());

        // then
        assertThat(found).containsExactly(saved);
    }

    @DisplayName("특정 날짜 이후의 예약을 찾는다.")
    @Test
    void dateStartFrom() {
        // given
        ReservationSearchSpecification spec = new ReservationSearchSpecification().dateStartFrom(
                TestFixture.TOMORROW);

        // when
        List<Reservation> found = reservationRepository.findAll(spec.build());

        // then
        assertThat(found).containsExactly(saved);
    }

    @DisplayName("특정 날짜 이전의 예약을 찾는다.")
    @Test
    void dateEndAt() {
        // given
        ReservationSearchSpecification spec = new ReservationSearchSpecification().dateEndAt(
                TestFixture.TOMORROW);

        // when
        List<Reservation> found = reservationRepository.findAll(spec.build());

        // then
        assertThat(found).containsExactly(saved);
    }
}
