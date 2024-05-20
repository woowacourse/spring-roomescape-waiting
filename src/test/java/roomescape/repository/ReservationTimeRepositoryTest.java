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

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationRepository.saveAll(List.of(
                new Reservation(
                        LocalDate.of(2000, 1, 1),
                        new ReservationTime(1L, LocalTime.of(1, 0)),
                        new Theme(1L, "n1", "d1", "t1"),
                        new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER)),
                new Reservation(
                        LocalDate.of(2000, 1, 2),
                        new ReservationTime(2L, LocalTime.of(2, 0)),
                        new Theme(2L, "n2", "d2", "t2"),
                        new Member(2L, "우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN))));
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
