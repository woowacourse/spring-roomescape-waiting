package roomescape.reservation.waiting.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.infrastructure.JpaReservationTimeRepository;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.JpaThemeRepository;

@DataJpaTest
class JpaWaitingReservationRepositoryTest {

    @Autowired
    private JpaWaitingReservationRepository jpaWaitingReservationRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @DisplayName("대기 예약을 순서와 함께 가져올 수 있다.")
    @Test
    void get_waiting_reservation_with_rank() {
        // given
        Theme theme = jpaThemeRepository.save(new Theme("test", "test", "test"));
        ReservationTime reservationTime = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Member member = jpaMemberRepository.save(
            new Member(new Name("test"), new Email("t@e.com"), new Password("ps")));

        WaitingReservation waiting1 = jpaWaitingReservationRepository.save(
            new WaitingReservation(LocalDate.now().plusDays(1), reservationTime, theme, member));
        WaitingReservation waiting2 = jpaWaitingReservationRepository.save(
            new WaitingReservation(LocalDate.now().plusDays(1), reservationTime, theme, member));

        // when
        List<WaitingReservationWithRank> waitings = jpaWaitingReservationRepository.findWaitingsWithRankByMember_Id(
            member.getId());

        // then
        Assertions.assertThat(waitings)
            .containsExactly(
                new WaitingReservationWithRank(waiting1, 0),
                new WaitingReservationWithRank(waiting2, 1)
            );
    }
}
