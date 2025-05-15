package roomescape.reservation.infrastructure.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.ReservationTestFixture;
import roomescape.member.model.Member;
import roomescape.reservation.infrastructure.db.dao.ReservationJpaRepository;
import roomescape.reservation.model.entity.Reservation;

@DataJpaTest
class ReservationJpaRepositoryTest {

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private ReservationDbRepository reservationDbRepository;

    @PersistenceContext
    private EntityManager em;

    @DisplayName("조건이 없다면 전체 조회한다.")
    @Test
    void selectByNonFilter() {
        Member member = ReservationTestFixture.getMemberFixture();
        em.persist(member);
        Reservation reservation = ReservationTestFixture.getReservationFixture(member);
        Reservation reservation1 = ReservationTestFixture.getReservationFixture(
            LocalDate.now().plusDays(5), member);
        reservationJpaRepository.saveAll(List.of(reservation, reservation1));
//
//        List<Reservation> searchReservations = reservationDbRepository.getSearchReservations(null,
//            null, null, null);
//
//        assertThat(searchReservations).hasSize(2);
//        assertThat(searchReservations.get(0).getId()).isEqualTo(1L);
//        assertThat(searchReservations.get(1).getId()).isEqualTo(2L);
    }

    @DisplayName("멤버 ID로 조회할 수 있다.")
    @Test
    void selectByFilterMemberId() {

        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
            "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
            "웨이드", LocalDate.now().plusDays(6), "1", "1", "2");
        List<Reservation> result = reservationH2Dao.selectByFilter(null, 1L, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("브라운");
    }

    @DisplayName("테마 ID로 조회할 수 있다.")
    @Test
    void selectByFilterThemeId() {
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
            "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
            "웨이드", LocalDate.now().plusDays(6), "1", "1", "2");
        List<Reservation> result = reservationH2Dao.selectByFilter(1L, null, null, null);

        assertThat(result).hasSize(2);
    }

    @DisplayName("기간 필터링으로 조회 가능하다.")
    @Test
    void selectByFilterDate() {
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
            "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
            "웨이드", LocalDate.now().plusDays(6), "1", "1", "2");
        List<Reservation> result = reservationH2Dao.selectByFilter(null, null,
            LocalDate.now().plusDays(6), LocalDate.now().plusDays(6));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("웨이드");
    }

    @DisplayName("조건에 맞는 값이 없다면 빈값을 반환한다.")
    @Test
    void selectByFilterEmpty() {
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
            "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        List<Reservation> result = reservationH2Dao.selectByFilter(null, 9999L, null, null);

        assertThat(result).isEmpty();
    }
}
