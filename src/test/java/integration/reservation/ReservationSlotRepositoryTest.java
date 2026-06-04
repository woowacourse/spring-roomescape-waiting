package integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static roomescape.domain.fixture.ReservationFixture.createCanceledEntry;
import static roomescape.domain.fixture.ReservationFixture.createEntry;
import static roomescape.domain.fixture.ReservationFixture.reservedReservationId;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationActiveStatus;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;
import roomescape.domain.fixture.ReservationFixture;
import roomescape.exception.DuplicateEntityException;
import roomescape.exception.EntityNotFoundException;
import roomescape.persistence.ReservationSlotRepository;
import roomescape.persistence.dto.ReservationCondition;

class ReservationSlotRepositoryTest extends BaseIntegrationTest {
    @Autowired
    private ReservationSlotRepository reservationRepository;
    @Autowired
    private ReservationDataSource dataSource;

    private ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0), TimeStatus.ACTIVE);
    private Theme theme = new Theme(1L, "공포", "어마무시한 공포 테마", "https://theme.com/image.png", false);

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
        dataSource.insertTheme(theme.getName(), theme.getDescription(), theme.getThumbnailImageUrl());
        dataSource.insertReservationTime(reservationTime.getStartAt());
    }

    @Test
    void 예약을_저장하면_예약_슬롯과_엔트리를_함께_저장한다() {
        // given
        ReservationSlot slot = ReservationFixture.createWithAll("이프", LocalDate.now().plusDays(1), theme, reservationTime);

        // when
        ReservationSlot saved = reservationRepository.save(slot);

        // then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(dataSource.hasReservationById(saved.getId())).isTrue();
        assertThat(saved.getReservations())
                .singleElement()
                .extracting(Reservation::getId, Reservation::getName, Reservation::getStatus)
                .containsExactly(1L, "이프", ReservationStatus.RESERVED);
    }

    @Test
    void 동일한_날짜와_시간으로_저장하면_DB_제약조건_에러가_발생한다() {
        // given
        ReservationSlot first = ReservationFixture.createWithAll("이프", LocalDate.now().plusDays(1), theme, reservationTime);
        ReservationSlot second = ReservationFixture.createWithAll("아루", LocalDate.now().plusDays(1), theme, reservationTime);
        reservationRepository.save(first);

        // when & then: DB의 UK Constraint 발생 후 비즈니스 예외로 변환
        assertThatThrownBy(() -> reservationRepository.save(second))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약이 존재하는 시간입니다");
    }

    @Test
    void 같은_슬롯에_같은_이름의_취소_이력을_여러_번_저장할_수_있다() {
        // given
        Reservation firstCanceled = createCanceledEntry(null, "이프", ReservationStatus.RESERVED);
        Reservation secondCanceled = createCanceledEntry(null, "이프", ReservationStatus.WAITING);

        ReservationSlot slot = new ReservationSlot(
                null,
                LocalDate.now().plusDays(1),
                theme,
                reservationTime,
                List.of(firstCanceled, secondCanceled)
        );

        // when
        ReservationSlot saved = reservationRepository.save(slot);

        // then
        assertThat(saved.getReservations())
                .extracting(Reservation::getName, Reservation::getStatus, Reservation::getActiveStatus)
                .containsExactlyInAnyOrder(
                        tuple("이프", ReservationStatus.RESERVED, ReservationActiveStatus.CANCELED),
                        tuple("이프", ReservationStatus.WAITING, ReservationActiveStatus.CANCELED)
                );
    }

    @Test
    void 같은_슬롯에_같은_이름의_활성_엔트리를_저장하면_DB_제약조건_에러가_발생한다() {
        // given
        ReservationSlot slot = new ReservationSlot(
                null,
                LocalDate.now().plusDays(1),
                theme,
                reservationTime,
                List.of(
                        createEntry(null, "이프", ReservationStatus.RESERVED),
                        createEntry(null, "이프", ReservationStatus.WAITING)
                )
        );

        // when & then
        assertThatThrownBy(() -> reservationRepository.save(slot))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 예약 또는 대기가 존재합니다.");
    }

    @Test
    void 예약_슬롯을_수정한다() {
        // given
        ReservationSlot saved = reservationRepository.save(
                ReservationFixture.createWithAll("이프", LocalDate.now().plusDays(2), theme, reservationTime)
        );

        ReservationSlot updated = new ReservationSlot(
                saved.getId(),
                LocalDate.now().plusDays(2),
                theme,
                reservationTime,
                List.of()
        );

        // when
        reservationRepository.save(updated);

        // then
        ReservationCondition condition = new ReservationCondition(updated.getDate(), theme.getId(), reservationTime.getId());
        Optional<ReservationSlot> find = reservationRepository.findByDateAndThemeAndTimeForUpdate(condition);

        assertThat(find).isPresent();
        assertThat(find.get().getDate()).isEqualTo(LocalDate.now().plusDays(2));
        assertThat(find.get().getReservations())
                .singleElement()
                .extracting(Reservation::getName, Reservation::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

    @Test
    void 존재하지_않는_예약_슬롯을_수정하면_예외가_발생한다() {
        // given
        ReservationSlot slot = new ReservationSlot(
                999L,
                LocalDate.now().plusDays(2),
                theme,
                reservationTime,
                List.of()
        );

        // when & then
        assertThatThrownBy(() -> reservationRepository.save(slot))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 예약 슬롯입니다.");
    }

    @Test
    void 예약_엔트리_상태를_수정한다() {
        // given
        ReservationSlot saved = reservationRepository.save(
                ReservationFixture.createWithAll("이프", LocalDate.now().plusDays(2), theme, reservationTime)
        );
        long reservationId = reservedReservationId(saved);

        // when
        saved.cancelReservation(reservationId);
        reservationRepository.save(saved);

        // then
        ReservationSlot find = reservationRepository.findByReservationIdForUpdate(reservationId).orElseThrow();
        assertThat(find.getReservations())
                .singleElement()
                .extracting(Reservation::getStatus, Reservation::getActiveStatus)
                .containsExactly(ReservationStatus.RESERVED, ReservationActiveStatus.CANCELED);
    }

    @Test
    void 날짜_테마_시간으로_예약을_조회하면_엔트리와_함께_반환된다() {
        // given
        ReservationSlot saved = reservationRepository.save(
                ReservationFixture.createWithAll("이프", LocalDate.now().plusDays(1), theme, reservationTime)
        );
        ReservationCondition condition = new ReservationCondition(saved.getDate(), theme.getId(), reservationTime.getId());

        // when
        Optional<ReservationSlot> find = reservationRepository.findByDateAndThemeAndTimeForUpdate(condition);

        // then
        assertThat(find).isPresent();
        ReservationSlot result = find.get();
        assertThat(result.getReservations())
                .singleElement()
                .extracting(Reservation::getName, Reservation::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
        assertThat(result.getReservations())
                .singleElement()
                .extracting(Reservation::getId)
                .isNotNull();
    }

    @Test
    void 예약_엔트리_ID로_예약을_조회하면_해당_예약의_모든_엔트리를_반환한다() {
        // given
        ReservationSlot saved = reservationRepository.save(
                ReservationFixture.createWithAll("이프", LocalDate.now().plusDays(1), theme, reservationTime)
        );

        // when
        Optional<ReservationSlot> find = reservationRepository.findByReservationIdForUpdate(reservedReservationId(saved));

        // then
        assertThat(find).isPresent();
        assertThat(find.get().getId()).isEqualTo(saved.getId());
        assertThat(find.get().getReservations())
                .singleElement()
                .extracting(Reservation::getName, Reservation::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

}
