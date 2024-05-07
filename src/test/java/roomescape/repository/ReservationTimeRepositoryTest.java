package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql("/truncate.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        themeRepository.saveAll(List.of(
                new Theme("n1", "d1", "t1"),
                new Theme("n2", "d2", "t2")));

        reservationTimeRepository.saveAll(List.of(
                new ReservationTime(LocalTime.of(1, 0)),
                new ReservationTime(LocalTime.of(2, 0))));

        memberRepository.saveAll(List.of(
                new Member("에버", "treeboss@gmail.com", "treeboss123!", Role.USER),
                new Member("우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN)));

        reservationRepository.saveAll(List.of(
                new Reservation(
                        LocalDate.of(2000,1,1),
                        new ReservationTime(1, null),
                        new Theme(1, null, null, null),
                        new Member(1, null, null, null, null)),
                new Reservation(LocalDate. of(2000, 1, 2),
                        new ReservationTime(2, null),
                        new Theme(2, null, null, null),
                        new Member(2, null, null, null, null))));
    }

    @DisplayName("특정 startAt을 가진 예약 시간이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_startAt() {
        boolean isExist = reservationTimeRepository.existsByStartAt(LocalTime.of(1, 0));
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 startAt을 가진 예약 시간이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_startAt() {
        boolean isExist = reservationTimeRepository.existsByStartAt(LocalTime.of(9, 0));
        assertThat(isExist).isFalse();
    }
}
