package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setUp() {
        waitingRepository.saveAll(List.of(
                new Waiting(
                        LocalDate.of(2000, 1, 1),
                        new ReservationTime(1L, LocalTime.of(1, 0)),
                        new Theme(1L, "n1", "d1", "t1"),
                        new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER)),
                new Waiting(
                        LocalDate.of(2000, 1, 2),
                        new ReservationTime(2L, LocalTime.of(2, 0)),
                        new Theme(2L, "n2", "d2", "t2"),
                        new Member(2L, "우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN))));
    }

    @DisplayName("특정 멤버의 예약을 조회한다.")
    @Test
    @Transactional // TODO: study about transaction
    void should_find_by_memberId() {
        List<Waiting> waiting = waitingRepository.findByMemberId(2L);
        assertAll(
                () -> assertThat(waiting).hasSize(1),
                () -> assertThat(waiting.get(0).getId()).isEqualTo(2L));
    }
}
