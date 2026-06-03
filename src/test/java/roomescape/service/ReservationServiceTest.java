package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.User;
import roomescape.dto.reservation.response.ReservationResponses;
import roomescape.dto.reservation.response.ReservationWithStatusResponses;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;
import roomescape.repository.ReservationRepository;

class ReservationServiceTest extends ServiceIntegrationTest {

    private static final long OTHER_STORE_ID = 2L;

    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationRepository reservationRepository;

    private User manager;

    @BeforeEach
    void setUp() {
        insertDefaultStore();
        long managerId = DbFixtures.insertManager(jdbcTemplate, "매니저");
        DbFixtures.assignManager(jdbcTemplate, DEFAULT_STORE_ID, managerId);
        manager = Fixtures.manager("매니저").withId(managerId);
    }

    @Test
    @DisplayName("createReservation - id가 채워진 도메인을 반환한다")
    void createReservationReturnsDomainWithId() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        Reservation created = service.create(
                Fixtures.createCommand(brown, themeId, Fixtures.daysFromNow(1), timeId), ReservationStatus.RESERVED);

        assertThat(created.getId()).isPositive();
        assertThat(created.getUser().getName()).isEqualTo("브라운");
        assertThat(created.getDate()).isEqualTo(Fixtures.daysFromNow(1));
        assertThat(created.getTheme().getId()).isEqualTo(themeId);
        assertThat(created.getTime().getId()).isEqualTo(timeId);
    }

    @Test
    @DisplayName("createReservation - 과거의 날짜 시간이면 예외")
    void createReservationThrowsWhenDateTimeIsPast() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("11:00");

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(brown, themeId, Fixtures.daysFromNow(-2), timeId), ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_DATE_TIME_RESERVATION);
    }

    @Test
    @DisplayName("createReservation - 같은 날짜/시간/테마 중복이면 예외")
    void createReservationThrowsWhenSlotIsDuplicated() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        service.create(
                Fixtures.createCommand(brown, themeId, Fixtures.daysFromNow(1), timeId), ReservationStatus.RESERVED);

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(other, themeId, Fixtures.daysFromNow(1), timeId), ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.DUPLICATE_RESERVATION);
    }

    @Test
    @DisplayName("createReservation - 존재하지 않는 themeId이면 ResourceNotFoundException")
    void createReservationThrowsResourceNotFoundExceptionWhenThemeIdDoesNotExist() {
        User brown = member("브라운");
        long timeId = time("10:00");

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(brown, 9999L, Fixtures.daysFromNow(1), timeId), ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("createReservation - 존재하지 않는 timeId이면 ResourceNotFoundException")
    void createReservationThrowsResourceNotFoundExceptionWhenTimeIdDoesNotExist() {
        User brown = member("브라운");
        long themeId = theme("공포");

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(brown, themeId, Fixtures.daysFromNow(1), 9999L), ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("getReservations - 다음 페이지가 있으면 hasNext가 true")
    void getReservationsHasNextTrueWhenNextPageExists() {
        User user = member("A");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-6));
        saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-5));
        saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-4));

        ReservationResponses responses = service.getReservations(0, 2, null, manager);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    @DisplayName("getReservations - 다음 페이지가 없으면 hasNext가 false")
    void getReservationsHasNextFalseWhenNoNextPage() {
        User user = member("A");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-6));
        saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-5));

        ReservationResponses responses = service.getReservations(0, 2, null, manager);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    @DisplayName("getReservations - name이 주어지면 해당 이름의 예약만 반환한다")
    void getReservationsReturnsOnlyMatchingNameWhenNameIsGiven() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-6));
        saveReservation(other, themeId, timeId, Fixtures.daysFromNow(-5));
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-4));

        ReservationResponses responses = service.getReservations(0, 10, "브라운", manager);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.reservations()).extracting("name").containsOnly("브라운");
    }

    @Test
    @DisplayName("getMyReservations - 본인 예약만 반환한다")
    void getMyReservationsReturnsOnlyOwnReservations() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-6));
        saveReservation(other, themeId, timeId, Fixtures.daysFromNow(-5));
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-4));

        ReservationWithStatusResponses responses = service.getMyReservations(brown, 0, 20);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.reservations()).extracting("name").containsOnly("브라운");
    }

    @Test
    @DisplayName("getMyReservations - size를 초과하면 hasNext가 true이고 size만큼만 반환한다")
    void getMyReservationsReturnsOnlySizeWithHasNextWhenExceedingSize() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-6));
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-5));
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-4));

        ReservationWithStatusResponses firstPage = service.getMyReservations(brown, 0, 2);
        ReservationWithStatusResponses secondPage = service.getMyReservations(brown, 1, 2);

        assertThat(firstPage.reservations()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(secondPage.reservations()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("getMyReservations - 예약과 대기가 없으면 빈 목록을 반환한다")
    void getMyReservationsReturnsEmptyWhenNoReservationsOrWaiting() {
        User brown = member("브라운");

        ReservationWithStatusResponses responses = service.getMyReservations(brown, 0, 20);

        assertThat(responses.reservations()).isEmpty();
        assertThat(responses.waitingReservations()).isEmpty();
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    @DisplayName("getMyReservations - 확정은 reservations에, 대기는 waitingReservations에 분리돼 노출된다")
    void getMyReservationsSeparatesReservedAndWaiting() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long timeId2 = time("11:00");
        long reservedId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-6));
        long waitingId = saveWaitingReservation(brown, themeId, timeId2, Fixtures.daysFromNow(-5));

        ReservationWithStatusResponses responses = service.getMyReservations(brown, 0, 20);

        assertThat(responses.reservations()).extracting("id").containsExactly(reservedId);
        assertThat(responses.waitingReservations()).extracting("id").containsExactly(waitingId);
        assertThat(responses.waitingReservations()).extracting("waitingOrder").containsExactly(1);
    }

    @Test
    @DisplayName("getMyReservations - 대기 순번은 슬롯별로 독립적으로 계산된다")
    void getMyReservationsCalculatesWaitingOrderPerSlot() {
        User brown = member("브라운");
        User charles = member("샤를");
        User aron = member("아론");
        long themeA = theme("A");
        long themeB = theme("B");
        long timeId = time("10:00");

        // 슬롯 A(06-01): 아론(먼저) → 브라운(나중) ⇒ 브라운 2번
        long slotAAron = saveWaitingReservation(aron, themeA, timeId, Fixtures.daysFromNow(25));
        long slotABrown = saveWaitingReservation(brown, themeA, timeId, Fixtures.daysFromNow(25));
        // 슬롯 B(06-02): 샤를 → 아론 → 브라운 ⇒ 브라운 3번
        long slotBCharles = saveWaitingReservation(charles, themeB, timeId, Fixtures.daysFromNow(26));
        long slotBAron = saveWaitingReservation(aron, themeB, timeId, Fixtures.daysFromNow(26));
        long slotBBrown = saveWaitingReservation(brown, themeB, timeId, Fixtures.daysFromNow(26));

        setCreatedAt(slotAAron, "2026-05-01 09:00:00");
        setCreatedAt(slotABrown, "2026-05-01 10:00:00");
        setCreatedAt(slotBCharles, "2026-05-01 09:00:00");
        setCreatedAt(slotBAron, "2026-05-01 10:00:00");
        setCreatedAt(slotBBrown, "2026-05-01 11:00:00");

        ReservationWithStatusResponses responses = service.getMyReservations(brown, 0, 20);

        assertThat(responses.waitingReservations()).extracting("id")
                .containsExactly(slotABrown, slotBBrown);
        assertThat(responses.waitingReservations()).extracting("waitingOrder")
                .containsExactly(2, 3);
    }

    @Test
    @DisplayName("getReservation - id로 단건을 조회한다")
    void getReservationFindsSingleById() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-1));

        Reservation found = service.getReservation(reservationId);

        assertThat(found.getId()).isEqualTo(reservationId);
        assertThat(found.getUser().getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("getReservation - 없는 id이면 ResourceNotFoundException")
    void getReservationThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThatThrownBy(() -> service.getReservation(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("cancelReservation - 없는 id이면 ResourceNotFoundException")
    void cancelReservationThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThatThrownBy(() -> service.cancelReservation(9999L, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("cancelReservation - 취소 후 조회되지 않는다")
    void cancelReservationMakesReservationUnqueryable() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, Fixtures.daysFromNow(1));

        service.cancelReservation(reservationId, manager);

        ReservationResponses responses = service.getReservations(0, 10, null, manager);
        assertThat(responses.reservations()).extracting("id").doesNotContain(reservationId);
    }

    @Test
    @DisplayName("cancelReservation - 담당하지 않는 매장 예약이면 StoreManagementForbiddenException")
    void cancelReservationThrowsStoreManagementForbiddenExceptionForUnmanagedStore() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        insertOtherStore();
        long reservationId = saveReservationInStore(user, themeId, timeId, Fixtures.daysFromNow(1), OTHER_STORE_ID);

        assertThatThrownBy(() -> service.cancelReservation(reservationId, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.STORE_MANAGEMENT_FORBIDDEN);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    @DisplayName("cancelReservation - 과거 예약이면 PastReservationModificationException")
    void cancelReservationThrowsPastReservationModificationExceptionWhenPast() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-1));

        assertThatThrownBy(() -> service.cancelReservation(reservationId, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_RESERVATION_MODIFICATION);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    @DisplayName("deletePastReservation - 과거 예약을 삭제하면 조회되지 않는다")
    void deletePastReservationMakesPastReservationUnqueryable() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-1));

        service.deletePastReservation(reservationId, manager);

        assertThat(reservationRepository.findById(reservationId)).isEmpty();
    }

    @Test
    @DisplayName("deletePastReservation - 아직 지나지 않은 예약이면 NonPastReservationDeletionException")
    void deletePastReservationThrowsNonPastReservationDeletionExceptionWhenNotPast() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, Fixtures.daysFromNow(1));

        assertThatThrownBy(() -> service.deletePastReservation(reservationId, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.NON_PAST_RESERVATION_DELETION);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    @DisplayName("deletePastReservation - 담당하지 않는 매장 예약이면 StoreManagementForbiddenException")
    void deletePastReservationThrowsStoreManagementForbiddenExceptionForUnmanagedStore() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        insertOtherStore();
        long reservationId = saveReservationInStore(user, themeId, timeId, Fixtures.daysFromNow(-1), OTHER_STORE_ID);

        assertThatThrownBy(() -> service.deletePastReservation(reservationId, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.STORE_MANAGEMENT_FORBIDDEN);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    @DisplayName("getReservations - 담당하는 매장의 예약만 반환한다")
    void getReservationsReturnsOnlyManagedStoreReservations() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long mine = saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-6));
        insertOtherStore();
        saveReservationInStore(user, themeId, timeId, Fixtures.daysFromNow(-5), OTHER_STORE_ID);

        ReservationResponses responses = service.getReservations(0, 10, null, manager);

        assertThat(responses.reservations()).extracting("id").containsExactly(mine);
    }

    @Test
    @DisplayName("getReservations - 담당 매장이 없으면 빈 목록")
    void getReservationsReturnsEmptyWhenNoManagedStore() {
        User stranger = member("무관리자");
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(user, themeId, timeId, Fixtures.daysFromNow(-6));

        ReservationResponses responses = service.getReservations(0, 10, null, stranger);

        assertThat(responses.reservations()).isEmpty();
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    @DisplayName("cancelOwnReservation - userId 불일치면 예외")
    void cancelOwnReservationThrowsWhenUserIdMismatch() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(1));

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(reservationId, other)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESERVATION_OWNER_MISMATCH);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    @DisplayName("cancelOwnReservation - 없는 id이면 ResourceNotFoundException")
    void cancelOwnReservationThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        User brown = member("브라운");

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(9999L, brown)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("cancelOwnReservation - 과거 예약이면 예외")
    void cancelOwnReservationThrowsWhenReservationIsPast() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-6));

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(reservationId, brown)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_RESERVATION_MODIFICATION);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    @DisplayName("updateOwnReservation - 변경된 도메인을 반환한다")
    void updateOwnReservationReturnsUpdatedDomain() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long themeId2 = theme("추리");
        long timeId = time("10:00");
        long timeId2 = time("11:00");
        long reservationId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(25));

        Reservation updated = service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId2, Fixtures.daysFromNow(26), timeId2));

        assertThat(updated.getDate()).isEqualTo(Fixtures.daysFromNow(26));
        assertThat(updated.getTheme().getId()).isEqualTo(themeId2);
        assertThat(updated.getTime().getId()).isEqualTo(timeId2);
    }

    @Test
    @DisplayName("updateOwnReservation - userId 불일치면 예외")
    void updateOwnReservationThrowsWhenUserIdMismatch() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(25));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, other, themeId, Fixtures.daysFromNow(26), timeId)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESERVATION_OWNER_MISMATCH);
    }

    @Test
    @DisplayName("updateOwnReservation - 기존 예약이 과거이면 예외")
    void updateOwnReservationThrowsWhenExistingReservationIsPast() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-6));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, Fixtures.daysFromNow(26), timeId)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_RESERVATION_MODIFICATION);
    }

    @Test
    @DisplayName("updateOwnReservation - 새 일정이 과거이면 예외")
    void updateOwnReservationThrowsWhenNewScheduleIsPast() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(25));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, Fixtures.daysFromNow(-6), timeId)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_DATE_TIME_RESERVATION);
    }

    @Test
    @DisplayName("updateOwnReservation - 새 일정이 다른 예약과 충돌하면 예외")
    void updateOwnReservationThrowsWhenNewScheduleConflictsWithOther() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long timeId2 = time("11:00");
        long reservationId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(25));
        saveReservation(other, themeId, timeId2, Fixtures.daysFromNow(26));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, Fixtures.daysFromNow(26), timeId2)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.DUPLICATE_RESERVATION);
    }

    @Test
    @DisplayName("updateOwnReservation - 기존 슬롯과 동일하면 예외 없이 통과")
    void updateOwnReservationPassesWhenSameAsExistingSlot() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(25));

        Reservation updated = service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, Fixtures.daysFromNow(25), timeId));

        assertThat(updated.getId()).isEqualTo(reservationId);
    }

    @Test
    @DisplayName("updateOwnReservation - 없는 id이면 ResourceNotFoundException")
    void updateOwnReservationThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(9999L, brown, themeId, Fixtures.daysFromNow(26), timeId)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("createWaitingReservation - 예약 대기를 생성한다")
    void createWaitingReservation() {
        User brown = member("브라운");
        User charles = member("샤를");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(25));

        Reservation result = service.create(
                Fixtures.createCommand(charles, themeId, Fixtures.daysFromNow(25), timeId), ReservationStatus.WAITING);

        assertThat(result.getId()).isPositive();
        assertThat(result.getDate()).isEqualTo(Fixtures.daysFromNow(25));
        assertThat(result.getTheme().getName()).isEqualTo("공포");
        assertThat(result.getUser().getName()).isEqualTo("샤를");
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    @DisplayName("createWaitingReservation - 해당 슬롯에 아직 예약 확정이 없으면 ReservationNotFoundForWaitingException을 반환한다")
    void createWaitingReservationThrowsReservationNotFoundForWaitingExceptionWhenNoConfirmed() {
        User charles = member("샤를");
        long themeId = theme("공포");
        long timeId = time("10:00");

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(charles, themeId, Fixtures.daysFromNow(25), timeId), ReservationStatus.WAITING))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESERVATION_NOT_FOUND_FOR_WAITING);
    }

    @Test
    @DisplayName("createWaitingReservation - 과거 날짜로 예약 대기를 걸면 PastDateTimeReservationException을 반환한다")
    void createWaitingReservationThrowsPastDateTimeReservationExceptionWhenPastDate() {
        User brown = member("브라운");
        User charles = member("샤를");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(-3650));

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(charles, themeId, Fixtures.daysFromNow(-3650), timeId), ReservationStatus.WAITING))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_DATE_TIME_RESERVATION);
    }

    @Test
    @DisplayName("createWaitingReservation - 이미 본인 예약 대기가 존재하면 예외")
    void createWaitingReservationThrowsWhenOwnWaitingAlreadyExists() {
        User brown = member("브라운");
        User charles = member("샤를");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, Fixtures.daysFromNow(25));
        saveWaitingReservation(charles, themeId, timeId, Fixtures.daysFromNow(25));

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(charles, themeId, Fixtures.daysFromNow(25), timeId), ReservationStatus.WAITING))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.DUPLICATE_WAITING_RESERVATION);
    }

    private User member(String name) {
        long id = DbFixtures.insertMember(jdbcTemplate, name);
        return Fixtures.member(name).withId(id);
    }

    private long theme(String name) {
        return DbFixtures.insertTheme(jdbcTemplate, name);
    }

    private long time(String startAt) {
        return DbFixtures.insertTime(jdbcTemplate, startAt);
    }

    private long saveReservation(User user, long themeId, long timeId, LocalDate date) {
        return DbFixtures.insertReservation(jdbcTemplate, user.getId(), themeId, date.toString(), timeId);
    }

    private long saveWaitingReservation(User user, long themeId, long timeId, LocalDate date) {
        return DbFixtures.insertReservation(jdbcTemplate, user.getId(), themeId, date.toString(), timeId, "WAITING");
    }

    private void setCreatedAt(long reservationId, String timestamp) {
        jdbcTemplate.update("update reservation set created_at = ? where id = ?", timestamp, reservationId);
    }

    private long saveReservationInStore(User user, long themeId, long timeId, LocalDate date, long storeId) {
        return DbFixtures.insertReservationInStore(jdbcTemplate, user.getId(), themeId, date.toString(), timeId,
                storeId);
    }

    private void insertOtherStore() {
        jdbcTemplate.update("INSERT INTO store(id, name) VALUES (?, ?)", OTHER_STORE_ID, "다른매장");
    }
}
