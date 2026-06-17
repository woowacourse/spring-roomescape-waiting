package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.dao.exception.DataConflictException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPayment;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

class ReservationPaymentDaoTest {

    private EmbeddedDatabase dataSource;
    private ReservationPaymentDao reservationPaymentDao;

    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
        reservationPaymentDao = new ReservationPaymentDao(new JdbcTemplate(dataSource));
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    @Test
    void save_결제_대기_예약을_저장한다() {
        ReservationPayment payment = payment("order_123456", LocalDate.of(2026, 12, 31), 1L, 1L);

        ReservationPayment saved = reservationPaymentDao.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderId()).isEqualTo("order_123456");
        assertThat(saved.getAmount()).isEqualTo(10000L);
    }

    @Test
    void findByOrderId_orderId로_결제_대기_예약을_조회한다() {
        reservationPaymentDao.save(payment("order_123456", LocalDate.of(2026, 12, 31), 1L, 1L));

        ReservationPayment found = reservationPaymentDao.findByOrderId("order_123456").orElseThrow();

        assertThat(found.getOrderId()).isEqualTo("order_123456");
        assertThat(found.getReservation().getName()).isEqualTo("브라운");
        assertThat(found.getReservation().getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(found.getReservation().getTheme().getName()).isEqualTo("공포의 저택");
    }

    @Test
    void save_동일한_orderId이면_예외() {
        ReservationPayment first = payment("order_123456", LocalDate.of(2026, 12, 31), 1L, 1L);
        ReservationPayment second = payment("order_123456", LocalDate.of(2026, 12, 30), 2L, 1L);
        reservationPaymentDao.save(first);

        assertThatThrownBy(() -> reservationPaymentDao.save(second))
                .isInstanceOf(DataConflictException.class);
    }

    @Test
    void save_동일한_슬롯이면_예외() {
        ReservationPayment first = payment("order_123456", LocalDate.of(2026, 12, 31), 1L, 1L);
        ReservationPayment second = payment("order_abcdef", LocalDate.of(2026, 12, 31), 1L, 1L);
        reservationPaymentDao.save(first);

        assertThatThrownBy(() -> reservationPaymentDao.save(second))
                .isInstanceOf(DataConflictException.class);
    }

    @Test
    void existsByDateAndTimeIdAndThemeId_결제_대기가_있으면_true() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        reservationPaymentDao.save(payment("order_123456", date, 1L, 1L));

        assertThat(reservationPaymentDao.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).isTrue();
    }

    private ReservationPayment payment(String orderId, LocalDate date, long timeId, long themeId) {
        ReservationTime time = new ReservationTime(timeId, LocalTime.of((int) timeId + 9, 0));
        Theme theme = new Theme(themeId, "공포의 저택", "설명", "https://example.com/img.jpg");
        Reservation reservation = new Reservation("브라운", date, LocalDateTime.of(2026, 12, 1, 12, 0), time, theme);
        return new ReservationPayment(orderId, 10000L, reservation);
    }
}
