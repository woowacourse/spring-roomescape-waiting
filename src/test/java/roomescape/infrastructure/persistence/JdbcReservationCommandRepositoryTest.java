package roomescape.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.PlayerName;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationCommandRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.ThemeName;

@JdbcTest
class JdbcReservationCommandRepositoryTest extends JdbcReservationTest {
    private final ReservationCommandRepository reservationCommandRepository;

    @Autowired
    public JdbcReservationCommandRepositoryTest(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.reservationCommandRepository = new JdbcReservationCommandRepository(jdbcTemplate);
    }

    @DisplayName("예약을 저장하면 id를 가진 예약을 저장 후 반환한다.")
    @Test
    void shouldReturnReservationWithIdWhenReservationSave() {
        ReservationTime reservationTime = reservationTimeRepository.create(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.create(new Theme(new ThemeName("theme1"), "desc", "url"));
        Member member = memberCommandRepository.create(new Member(new PlayerName("test"), new Email("test@email.com"), new Password("wootecoCrew6!"), Role.BASIC));
        Reservation reservationWithoutId = new Reservation(
                member,
                LocalDate.of(2024, 12, 25),
                reservationTime,
                theme
        );
        Reservation reservationWithId = reservationCommandRepository.create(reservationWithoutId);
        int totalRowCount = getTotalRowCount();
        assertAll(
                () -> assertThat(reservationWithId.getId()).isNotNull(),
                () -> assertThat(totalRowCount).isEqualTo(1)
        );
    }

    @DisplayName("id로 예약을 삭제한다.")
    @Test
    void shouldDeleteReservationWhenReservationIdExist() {
        long id = createReservation().getId();
        reservationCommandRepository.deleteById(id);
        int totalRowCount = getTotalRowCount();
        assertThat(totalRowCount).isZero();
    }


    private int getTotalRowCount() {
        String sql = "select count(*) from reservation";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
