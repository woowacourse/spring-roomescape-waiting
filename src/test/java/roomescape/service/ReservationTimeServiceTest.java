package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.ReservationTime;
import roomescape.dto.reservationtime.command.CreateReservationTimeCommand;
import roomescape.dto.reservationtime.response.ReservationTimeResponses;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;

class ReservationTimeServiceTest extends ServiceIntegrationTest {

    @Autowired
    private ReservationTimeService service;

    @Test
    @DisplayName("createReservationTime - 저장된 시간을 id와 함께 반환한다")
    void createReservationTimeReturnsSavedTimeWithId() {
        CreateReservationTimeCommand request = new CreateReservationTimeCommand(LocalTime.of(10, 0));

        ReservationTime created = service.createReservationTime(request);

        assertThat(created.getId()).isPositive();
        assertThat(created.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("getReservationTime - id로 단건을 조회한다")
    void getReservationTimeFindsSingleById() {
        long id = DbFixtures.insertTime(jdbcTemplate, "10:30");

        ReservationTime found = service.getReservationTime(id);

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getStartAt()).isEqualTo(LocalTime.of(10, 30));
    }

    @Test
    @DisplayName("getReservationTime - 없는 id이면 ResourceNotFoundException")
    void getReservationTimeThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThatThrownBy(() -> service.getReservationTime(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("getReservationTimes - 다음 페이지가 있으면 hasNext가 true")
    void getReservationTimesHasNextTrueWhenNextPageExists() {
        DbFixtures.insertTime(jdbcTemplate, "10:00");
        DbFixtures.insertTime(jdbcTemplate, "11:00");
        DbFixtures.insertTime(jdbcTemplate, "12:00");

        ReservationTimeResponses responses = service.getReservationTimes(0, 2);

        assertThat(responses.times()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    @DisplayName("getReservationTimes - 다음 페이지가 없으면 hasNext가 false")
    void getReservationTimesHasNextFalseWhenNoNextPage() {
        DbFixtures.insertTime(jdbcTemplate, "10:00");
        DbFixtures.insertTime(jdbcTemplate, "11:00");

        ReservationTimeResponses responses = service.getReservationTimes(0, 2);

        assertThat(responses.times()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    @DisplayName("deleteReservationTime - 없는 id이면 ResourceNotFoundException")
    void deleteReservationTimeThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThatThrownBy(() -> service.deleteReservationTime(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteReservationTime - 참조하는 예약이 없으면 정상 삭제")
    void deleteReservationTimeDeletesWhenNotReferenced() {
        long id = DbFixtures.insertTime(jdbcTemplate, "10:00");

        service.deleteReservationTime(id);

        ReservationTimeResponses responses = service.getReservationTimes(0, 10);
        assertThat(responses.times()).extracting("id").doesNotContain(id);
    }

    @Test
    @DisplayName("deleteReservationTime - 해당 시간을 참조하는 예약이 존재하면 예외")
    void deleteReservationTimeThrowsWhenReferencedByReservation() {
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        saveReservationWithTime(timeId);

        assertThatThrownBy(() -> service.deleteReservationTime(timeId))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESERVATION_TIME_IN_USE);
    }

    @Test
    @DisplayName("deleteReservationTime - 다른 시간을 참조하는 예약만 있으면 정상 삭제")
    void deleteReservationTimeDeletesWhenOnlyOtherTimeIsReferenced() {
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
        DbFixtures.insertReservation(jdbcTemplate, userId, themeId, Fixtures.daysFromNow(1).toString(), timeId);
    }
}
