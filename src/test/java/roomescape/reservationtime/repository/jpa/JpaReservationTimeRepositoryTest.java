package roomescape.reservationtime.repository.jpa;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.jpa.JpaMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.jpa.JpaReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.jpa.JpaThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles("test")
@DataJpaTest
class JpaReservationTimeRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    private Member member;
    private Theme theme;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        member = jpaMemberRepository.save(new Member(null, "test", "test@test.com", MemberRole.USER, "testpassword"));
        theme = jpaThemeRepository.save(new Theme(null, "test theme", "test description", "test thumbnail"));
        time = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
    }

    @Test
    void 예약_시간_생성() {
        ReservationTime savedReservationTime = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.of(12, 0)));
        assertThatCode(() -> jpaReservationTimeRepository.findById(savedReservationTime.getId())).doesNotThrowAnyException();
    }

    @Test
    void 예약_가능한_시간_목록_확인() {
        jpaReservationTimeRepository.saveAll(createTimes());
        jpaReservationRepository.save(new Reservation(null, LocalDate.now().plusDays(1), time, theme, member));
        List<ReservationTime> availableReservationTimes = jpaReservationTimeRepository.findAllByReservationDateAndThemeId(LocalDate.now().plusDays(1), theme.getId());
        assertAll(
                () -> assertThat(availableReservationTimes).hasSize(3),
                () -> assertThat(availableReservationTimes).doesNotContain(time)
        );
    }


    private List<ReservationTime> createTimes() {
        return List.of(
                new ReservationTime(null, LocalTime.of(12, 0)),
                new ReservationTime(null, LocalTime.of(14, 0)),
                new ReservationTime(null, LocalTime.of(16, 0))
        );
    }

}
