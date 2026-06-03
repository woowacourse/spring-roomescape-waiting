package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.ReservationTime;
import roomescape.dto.reservationtime.command.CreateReservationTimeCommand;
import roomescape.dto.reservationtime.response.ReservationTimeResponses;
import roomescape.fixture.DbFixtures;

class ReservationTimeServiceTest extends ServiceIntegrationTest {

    @Autowired
    private ReservationTimeService service;

    @Test
    void createReservationTime_저장된_시간을_id와_함께_반환한다() {
        CreateReservationTimeCommand request = new CreateReservationTimeCommand(LocalTime.of(10, 0));

        ReservationTime created = service.createReservationTime(request);

        assertThat(created.getId()).isPositive();
        assertThat(created.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void getReservationTime_id로_단건을_조회한다() {
        long id = DbFixtures.insertTime(jdbcTemplate, "10:30");

        ReservationTime found = service.getReservationTime(id);

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getStartAt()).isEqualTo(LocalTime.of(10, 30));
    }

    @Test
    void getReservationTime_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.getReservationTime(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void getReservationTimes_다음_페이지가_있으면_hasNext가_true() {
        DbFixtures.insertTime(jdbcTemplate, "10:00");
        DbFixtures.insertTime(jdbcTemplate, "11:00");
        DbFixtures.insertTime(jdbcTemplate, "12:00");

        ReservationTimeResponses responses = service.getReservationTimes(0, 2);

        assertThat(responses.times()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    void getReservationTimes_다음_페이지가_없으면_hasNext가_false() {
        DbFixtures.insertTime(jdbcTemplate, "10:00");
        DbFixtures.insertTime(jdbcTemplate, "11:00");

        ReservationTimeResponses responses = service.getReservationTimes(0, 2);

        assertThat(responses.times()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void deleteReservationTime_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.deleteReservationTime(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void deleteReservationTime_참조하는_예약이_없으면_정상_삭제() {
        long id = DbFixtures.insertTime(jdbcTemplate, "10:00");

        service.deleteReservationTime(id);

        ReservationTimeResponses responses = service.getReservationTimes(0, 10);
        assertThat(responses.times()).extracting("id").doesNotContain(id);
    }

    @Test
    void deleteReservationTime_해당_시간을_참조하는_예약이_존재하면_예외() {
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        saveReservationWithTime(timeId);

        assertThatThrownBy(() -> service.deleteReservationTime(timeId))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESERVATION_TIME_IN_USE);
    }

    @Test
    void deleteReservationTime_다른_시간을_참조하는_예약만_있으면_정상_삭제() {
        long usedTimeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long targetTimeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        saveReservationWithTime(usedTimeId);

        service.deleteReservationTime(targetTimeId);

        ReservationTimeResponses responses = service.getReservationTimes(0, 10);
        assertThat(responses.times()).extracting("id").doesNotContain(targetTimeId);
    }

    private void saveReservationWithTime(long timeId) {
        long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-08", timeId);
    }
}
