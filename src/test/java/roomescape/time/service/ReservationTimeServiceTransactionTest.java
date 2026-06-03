package roomescape.time.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.service.dto.ReservationTimeCommand;
import roomescape.time.service.dto.ReservationTimeResult;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = NONE)
public class ReservationTimeServiceTransactionTest {

    @Autowired
    ReservationTimeService reservationTimeService;

    @MockitoSpyBean
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM reservation_time");
    }

    @Test
    void save_rollback() {
        // given
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));

        doThrow(new RuntimeException("저장 중 에러 발생"))
                .when(reservationTimeRepository)
                .save(any(ReservationTime.class));

        // when
        assertThatThrownBy(() -> reservationTimeService.save(command))
                .isInstanceOf(RuntimeException.class);

        // then
        boolean exists = reservationTimeRepository.existsByStartAt(new ReservationTime(LocalTime.of(10, 0)));
        Assertions.assertFalse(exists);
    }

    @Test
    void delete_rollback() {
        // given
        ReservationTimeCommand command = new ReservationTimeCommand(LocalTime.of(10, 0));
        ReservationTimeResult savedTime = reservationTimeService.save(command);

        doThrow(new RuntimeException("삭제 중 에러 발생"))
                .when(reservationTimeRepository)
                .delete(any(ReservationTime.class));

        // when
        assertThatThrownBy(() -> reservationTimeService.deleteById(savedTime.id()))
                .isInstanceOf(RuntimeException.class);

        // then
        Optional<ReservationTime> result = reservationTimeRepository.findById(savedTime.id());
        Assertions.assertTrue(result.isPresent());
    }
}
