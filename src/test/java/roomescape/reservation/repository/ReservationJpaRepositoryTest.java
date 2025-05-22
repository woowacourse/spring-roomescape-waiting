package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.member.role.Role.ADMIN;
import static roomescape.reservation.fixture.ReservationDateFixture.예약날짜_내일;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.service.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@DataJpaTest
class ReservationJpaRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    EntityManager em;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), ADMIN));
        ReservationTime time = reservationTimeRepository.save(
                ReservationTime.create(LocalTime.of(10, 0))
        );
        Theme theme = themeRepository.save(new Theme("공포", "ss", "ss"));
        reservationRepository.save(
                Reservation.create(예약날짜_내일.getDate(), time, theme, member));
        reservationRepository.save(
                Reservation.create(LocalDate.of(2002, 5, 1), time, theme, member));
    }

    @Test
    void 선택한_옵션별_예약을_조회한다() {
        //when
        List<Reservation> reservations = reservationRepository.findByFilter(1L, 1L, LocalDate.now(),
                LocalDate.now().plusDays(2));

        //then
        assertThat(reservations.size()).isEqualTo(1);

    }

}
