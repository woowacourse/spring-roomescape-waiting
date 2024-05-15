package roomescape.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.ReservationTime;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationTimeRepositoryTest {
    @Autowired
    ReservationTimeRepository sut;

    @BeforeEach
    void setup(){
        sut.deleteAll();
    }

    @Test
    void create() {
        final var result = sut.save(ReservationTime.from(null, "10:00"));
        assertThat(result).isNotNull();
    }

    @Test
    void isExistByStartAt() {
        sut.save(ReservationTime.from(null, "10:00"));
        final var result = sut.existsByStartAt(LocalTime.parse("10:00"));
        assertThat(result).isTrue();
    }

    @Test
    void findById() {
        final var id = sut.save(ReservationTime.from(null, "10:00"))
                .getId();
        final var result = sut.findById(id);
        assertThat(result).contains(ReservationTime.from(null, "10:00"));
    }
    @Test
    void delete(){
        final var reservationTime = sut.save(ReservationTime.from(null, "10:00"));
        sut.delete(reservationTime);
        final var result = sut.findById(reservationTime.getId());
        assertThat(result).isNotPresent();
    }
    @Test
    void getAll(){
        sut.save(ReservationTime.from(null, "10:00"));
        sut.save(ReservationTime.from(null, "11:00"));

        final var result = sut.findAll();
        assertThat(result).hasSize(2);
    }
}
