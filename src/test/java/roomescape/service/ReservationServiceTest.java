package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
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
    void createReservation_id가_채워진_도메인을_반환한다() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        Reservation created = service.create(
                Fixtures.createCommand(brown, themeId, LocalDate.of(2026, 5, 8), timeId), ReservationStatus.RESERVED);

        assertThat(created.getId()).isPositive();
        assertThat(created.getUser().getName()).isEqualTo("브라운");
        assertThat(created.getDate()).isEqualTo(LocalDate.of(2026, 5, 8));
        assertThat(created.getTheme().getId()).isEqualTo(themeId);
        assertThat(created.getTime().getId()).isEqualTo(timeId);
    }

    @Test
    void createReservation_과거의_날짜_시간이면_예외() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("11:00");

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(brown, themeId, LocalDate.of(2026, 5, 5), timeId), ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_DATE_TIME_RESERVATION);
    }

    @Test
    void createReservation_같은_날짜시간테마_중복이면_예외() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        service.create(
                Fixtures.createCommand(brown, themeId, LocalDate.of(2026, 5, 8), timeId), ReservationStatus.RESERVED);

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(other, themeId, LocalDate.of(2026, 5, 8), timeId), ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.DUPLICATE_RESERVATION);
    }

    @Test
    void createReservation_존재하지_않는_themeId이면_ResourceNotFoundException() {
        User brown = member("브라운");
        long timeId = time("10:00");

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(brown, 9999L, LocalDate.of(2026, 5, 8), timeId), ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void createReservation_존재하지_않는_timeId이면_ResourceNotFoundException() {
        User brown = member("브라운");
        long themeId = theme("공포");

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(brown, themeId, LocalDate.of(2026, 5, 8), 9999L), ReservationStatus.RESERVED))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void getReservations_다음_페이지가_있으면_hasNext가_true() {
        User user = member("A");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1));
        saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 2));
        saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 3));

        ReservationResponses responses = service.getReservations(0, 2, null, manager);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.hasNext()).isTrue();
    }

    @Test
    void getReservations_다음_페이지가_없으면_hasNext가_false() {
        User user = member("A");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1));
        saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 2));

        ReservationResponses responses = service.getReservations(0, 2, null, manager);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void getReservations_name이_주어지면_해당_이름의_예약만_반환한다() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1));
        saveReservation(other, themeId, timeId, LocalDate.of(2026, 5, 2));
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 3));

        ReservationResponses responses = service.getReservations(0, 10, "브라운", manager);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.reservations()).extracting("name").containsOnly("브라운");
    }

    @Test
    void getMyReservations_본인_예약만_반환한다() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1));
        saveReservation(other, themeId, timeId, LocalDate.of(2026, 5, 2));
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 3));

        ReservationWithStatusResponses responses = service.getMyReservations(brown, 0, 20);

        assertThat(responses.reservations()).hasSize(2);
        assertThat(responses.reservations()).extracting("name").containsOnly("브라운");
    }

    @Test
    void getMyReservations_size를_초과하면_hasNext가_true이고_size만큼만_반환한다() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1));
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 2));
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 3));

        ReservationWithStatusResponses firstPage = service.getMyReservations(brown, 0, 2);
        ReservationWithStatusResponses secondPage = service.getMyReservations(brown, 1, 2);

        assertThat(firstPage.reservations()).hasSize(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(secondPage.reservations()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    void getMyReservations_예약과_대기가_없으면_빈_목록을_반환한다() {
        User brown = member("브라운");

        ReservationWithStatusResponses responses = service.getMyReservations(brown, 0, 20);

        assertThat(responses.reservations()).isEmpty();
        assertThat(responses.waitingReservations()).isEmpty();
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void getMyReservations_확정은_reservations에_대기는_waitingReservations에_분리돼_노출된다() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long timeId2 = time("11:00");
        long reservedId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1));
        long waitingId = saveWaitingReservation(brown, themeId, timeId2, LocalDate.of(2026, 5, 2));

        ReservationWithStatusResponses responses = service.getMyReservations(brown, 0, 20);

        assertThat(responses.reservations()).extracting("id").containsExactly(reservedId);
        assertThat(responses.waitingReservations()).extracting("id").containsExactly(waitingId);
        assertThat(responses.waitingReservations()).extracting("waitingOrder").containsExactly(1);
    }

    @Test
    void getMyReservations_대기_순번은_슬롯별로_독립적으로_계산된다() {
        User brown = member("브라운");
        User charles = member("샤를");
        User aron = member("아론");
        long themeA = theme("A");
        long themeB = theme("B");
        long timeId = time("10:00");

        // 슬롯 A(06-01): 아론(먼저) → 브라운(나중) ⇒ 브라운 2번
        long slotAAron = saveWaitingReservation(aron, themeA, timeId, LocalDate.of(2026, 6, 1));
        long slotABrown = saveWaitingReservation(brown, themeA, timeId, LocalDate.of(2026, 6, 1));
        // 슬롯 B(06-02): 샤를 → 아론 → 브라운 ⇒ 브라운 3번
        long slotBCharles = saveWaitingReservation(charles, themeB, timeId, LocalDate.of(2026, 6, 2));
        long slotBAron = saveWaitingReservation(aron, themeB, timeId, LocalDate.of(2026, 6, 2));
        long slotBBrown = saveWaitingReservation(brown, themeB, timeId, LocalDate.of(2026, 6, 2));

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
    void getReservation_id로_단건을_조회한다() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 6));

        Reservation found = service.getReservation(reservationId);

        assertThat(found.getId()).isEqualTo(reservationId);
        assertThat(found.getUser().getName()).isEqualTo("브라운");
    }

    @Test
    void getReservation_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.getReservation(9999L))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void cancelReservation_없는_id이면_ResourceNotFoundException() {
        assertThatThrownBy(() -> service.cancelReservation(9999L, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void cancelReservation_취소후_조회되지_않는다() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 8));

        service.cancelReservation(reservationId, manager);

        ReservationResponses responses = service.getReservations(0, 10, null, manager);
        assertThat(responses.reservations()).extracting("id").doesNotContain(reservationId);
    }

    @Test
    void cancelReservation_담당하지_않는_매장_예약이면_StoreManagementForbiddenException() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        insertOtherStore();
        long reservationId = saveReservationInStore(user, themeId, timeId, LocalDate.of(2026, 5, 8), OTHER_STORE_ID);

        assertThatThrownBy(() -> service.cancelReservation(reservationId, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.STORE_MANAGEMENT_FORBIDDEN);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void cancelReservation_과거_예약이면_PastReservationModificationException() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 6));

        assertThatThrownBy(() -> service.cancelReservation(reservationId, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_RESERVATION_MODIFICATION);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void deletePastReservation_과거_예약을_삭제하면_조회되지_않는다() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 6));

        service.deletePastReservation(reservationId, manager);

        assertThat(reservationRepository.findById(reservationId)).isEmpty();
    }

    @Test
    void deletePastReservation_아직_지나지_않은_예약이면_NonPastReservationDeletionException() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 8));

        assertThatThrownBy(() -> service.deletePastReservation(reservationId, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.NON_PAST_RESERVATION_DELETION);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void deletePastReservation_담당하지_않는_매장_예약이면_StoreManagementForbiddenException() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        insertOtherStore();
        long reservationId = saveReservationInStore(user, themeId, timeId, LocalDate.of(2026, 5, 6), OTHER_STORE_ID);

        assertThatThrownBy(() -> service.deletePastReservation(reservationId, manager))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.STORE_MANAGEMENT_FORBIDDEN);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void getReservations_담당하는_매장의_예약만_반환한다() {
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long mine = saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1));
        insertOtherStore();
        saveReservationInStore(user, themeId, timeId, LocalDate.of(2026, 5, 2), OTHER_STORE_ID);

        ReservationResponses responses = service.getReservations(0, 10, null, manager);

        assertThat(responses.reservations()).extracting("id").containsExactly(mine);
    }

    @Test
    void getReservations_담당_매장이_없으면_빈_목록() {
        User stranger = member("무관리자");
        User user = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(user, themeId, timeId, LocalDate.of(2026, 5, 1));

        ReservationResponses responses = service.getReservations(0, 10, null, stranger);

        assertThat(responses.reservations()).isEmpty();
        assertThat(responses.hasNext()).isFalse();
    }

    @Test
    void cancelOwnReservation_userId_불일치면_예외() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 8));

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(reservationId, other)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESERVATION_OWNER_MISMATCH);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void cancelOwnReservation_없는_id이면_ResourceNotFoundException() {
        User brown = member("브라운");

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(9999L, brown)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void cancelOwnReservation_과거_예약이면_예외() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1));

        assertThatThrownBy(() -> service.cancelOwnReservation(Fixtures.cancelCommand(reservationId, brown)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_RESERVATION_MODIFICATION);
        assertThat(reservationRepository.findById(reservationId)).isPresent();
    }

    @Test
    void updateOwnReservation_변경된_도메인을_반환한다() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long themeId2 = theme("추리");
        long timeId = time("10:00");
        long timeId2 = time("11:00");
        long reservationId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1));

        Reservation updated = service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId2, LocalDate.of(2026, 6, 2), timeId2));

        assertThat(updated.getDate()).isEqualTo(LocalDate.of(2026, 6, 2));
        assertThat(updated.getTheme().getId()).isEqualTo(themeId2);
        assertThat(updated.getTime().getId()).isEqualTo(timeId2);
    }

    @Test
    void updateOwnReservation_userId_불일치면_예외() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, other, themeId, LocalDate.of(2026, 6, 2), timeId)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESERVATION_OWNER_MISMATCH);
    }

    @Test
    void updateOwnReservation_기존_예약이_과거이면_예외() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 5, 1));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, LocalDate.of(2026, 6, 2), timeId)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_RESERVATION_MODIFICATION);
    }

    @Test
    void updateOwnReservation_새_일정이_과거이면_예외() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, LocalDate.of(2026, 5, 1), timeId)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_DATE_TIME_RESERVATION);
    }

    @Test
    void updateOwnReservation_새_일정이_다른_예약과_충돌하면_예외() {
        User brown = member("브라운");
        User other = member("다른사람");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long timeId2 = time("11:00");
        long reservationId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1));
        saveReservation(other, themeId, timeId2, LocalDate.of(2026, 6, 2));

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, LocalDate.of(2026, 6, 2), timeId2)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.DUPLICATE_RESERVATION);
    }

    @Test
    void updateOwnReservation_기존_슬롯과_동일하면_예외없이_통과() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");
        long reservationId = saveReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1));

        Reservation updated = service.updateOwnReservation(
                Fixtures.updateCommand(reservationId, brown, themeId, LocalDate.of(2026, 6, 1), timeId));

        assertThat(updated.getId()).isEqualTo(reservationId);
    }

    @Test
    void updateOwnReservation_없는_id이면_ResourceNotFoundException() {
        User brown = member("브라운");
        long themeId = theme("공포");
        long timeId = time("10:00");

        assertThatThrownBy(() -> service.updateOwnReservation(
                Fixtures.updateCommand(9999L, brown, themeId, LocalDate.of(2026, 6, 2), timeId)))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESOURCE_NOT_FOUND);
    }

    @Test
    void createWaitingReservation_예약_대기를_생성한다() {
        User brown = member("브라운");
        User charles = member("샤를");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1));

        Reservation result = service.create(
                Fixtures.createCommand(charles, themeId, LocalDate.of(2026, 6, 1), timeId), ReservationStatus.WAITING);

        assertThat(result.getId()).isPositive();
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getTheme().getName()).isEqualTo("공포");
        assertThat(result.getUser().getName()).isEqualTo("샤를");
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void createWaitingReservation_해당_슬롯에_아직_예약_확정이_없는_경우_ReservationNotFoundForWaitingException를_반환한다() {
        User charles = member("샤를");
        long themeId = theme("공포");
        long timeId = time("10:00");

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(charles, themeId, LocalDate.of(2026, 6, 1), timeId), ReservationStatus.WAITING))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.RESERVATION_NOT_FOUND_FOR_WAITING);
    }

    @Test
    void createWaitingReservation_과거_날짜로_예약_대기를_거는_경우_PastDateTimeReservationException를_반환한다() {
        User brown = member("브라운");
        User charles = member("샤를");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, LocalDate.of(1, 5, 1));

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(charles, themeId, LocalDate.of(1, 5, 1), timeId), ReservationStatus.WAITING))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.PAST_DATE_TIME_RESERVATION);
    }

    @Test
    void createWaitingReservation_이미_본인_예약_대기가_존재하면_예외() {
        User brown = member("브라운");
        User charles = member("샤를");
        long themeId = theme("공포");
        long timeId = time("10:00");
        saveReservation(brown, themeId, timeId, LocalDate.of(2026, 6, 1));
        saveWaitingReservation(charles, themeId, timeId, LocalDate.of(2026, 6, 1));

        assertThatThrownBy(() -> service.create(
                Fixtures.createCommand(charles, themeId, LocalDate.of(2026, 6, 1), timeId), ReservationStatus.WAITING))
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
