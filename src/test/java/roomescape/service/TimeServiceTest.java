package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.dto.request.TimeRequest;
import roomescape.dto.response.TimeResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.jpa.JpaMemberRepository;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;

@DataJpaTest
public class TimeServiceTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JpaMemberRepository memberRepository;
    @Autowired
    private JpaReservationRepository reservationRepository;
    @Autowired
    private JpaThemeRepository themeRepository;

    private TimeService timeService;

    @BeforeEach
    void setUp() {
        timeService = new TimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    @DisplayName("예약 여부와 함께 날짜, 테마에 해당하는 시간을 조회할 수 있다.")
    void findAllTimesWithBooked() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time1 = saveTime(LocalTime.of(10, 0));
        ReservationTime time2 = saveTime(LocalTime.of(12, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member, date, time1, theme);
        reservationRepository.save(reservation);

        List<TimeResponse> responses = timeService.findAllTimesWithBooked(date, theme.getId());

        assertAll(() -> {
            assertThat(responses.getFirst().alreadyBooked()).isTrue();
            assertThat(responses.getLast().alreadyBooked()).isFalse();
        });
    }

    @Test
    @DisplayName("시간을 추가할 수 있다.")
    void addTime() {
        TimeRequest request = new TimeRequest(LocalTime.of(10, 0));

        assertThat(timeService.addReservationTime(request)).isNotNull();
    }

    @Test
    @DisplayName("중복된 시간을 추가하면 예외가 발생한다.")
    void addDuplicatedTime() {
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(time);

        TimeRequest request = new TimeRequest(LocalTime.of(10, 0));

        assertThatThrownBy(() -> timeService.addReservationTime(request))
            .isInstanceOf(DuplicatedException.class)
            .hasMessageContaining("reservationTime");
    }

    private Member saveMember(Long tmp) {
        Member member = Member.createUser("이름" + tmp, "이메일" + tmp, "비밀번호" + tmp);
        memberRepository.save(member);

        return member;
    }

    private Theme saveTheme(Long tmp) {
        Theme theme = new Theme("이름" + tmp, "설명" + tmp, "썸네일" + tmp);
        themeRepository.save(theme);

        return theme;
    }

    private ReservationTime saveTime(LocalTime reservationTime) {
        ReservationTime time = new ReservationTime(reservationTime);
        reservationTimeRepository.save(time);

        return time;
    }
}
