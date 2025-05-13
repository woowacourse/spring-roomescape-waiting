package roomescape.reservation.infrastructure.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ThemeRequest;

@JdbcTest
public class ReservationDaoTest {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReservationDaoTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationDao = new ReservationDao(jdbcTemplate);
        this.reservationTimeDao = new ReservationTimeDao(jdbcTemplate);
        this.themeDao = new ThemeDao(jdbcTemplate);
    }

    @Test
    @DisplayName("예약 추가 확인 테스트")
    void insertTest() {
        // given
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(15, 40));
        ThemeRequest themeRequest = new ThemeRequest(
                "레벨2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );
        CreateReservationRequest createReservationRequest = new CreateReservationRequest(
                new Member(2L, "admin@admin.com", "admin", "어드민", Role.ADMIN),
                themeDao.insert(themeRequest),
                new ReservationDate(LocalDate.of(2023, 8, 5)),
                reservationTimeDao.insert(reservationTimeRequest.getStartAt())
        );

        // when
        reservationDao.insert(createReservationRequest);

        // then
        assertThat(count()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약 삭제 확인 테스트")
    void deleteTest() {
        // when
        reservationDao.delete(0L);

        // then
        assertThat(count()).isEqualTo(0);
    }

    private int count() {
        String sql = "select count(*) from reservation";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

}
