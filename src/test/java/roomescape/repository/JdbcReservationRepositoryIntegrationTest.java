package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;
import roomescape.domain.fixture.ReservationFixture;
import roomescape.exception.DuplicateEntityException;
import roomescape.repository.dto.ReservationCondition;
import roomescape.support.IntegrationTest;
import roomescape.support.TestDateTimes;

@IntegrationTest
@Sql("/integration-fixture.sql")
class JdbcReservationRepositoryIntegrationTest {

    private final ReservationTime reservationTime = ReservationTime.restore(1L, TestDateTimes.defaultTime(),
            TimeStatus.ACTIVE);
    private final Theme theme = Theme.restore(1L, "공포", "어마무시한 공포 테마", "https://theme.com/image.png", 30000L, false);
    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 예약을_저장하면_예약_슬롯과_엔트리를_함께_저장한다() {
        // given
        Reservation reservation = ReservationFixture.createWithAll("이프", TestDateTimes.tomorrow(), theme,
                reservationTime);

        // when
        Reservation saved = reservationRepository.save(reservation);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEntries())
                .singleElement()
                .extracting(ReservationEntry::getId, ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly(1L, "이프", ReservationStatus.RESERVED);
    }

    @Test
    void 동일한_날짜와_시간으로_저장하면_DB_제약조건_에러가_발생한다() {
        // given
        Reservation first = ReservationFixture.createWithAll("이프", TestDateTimes.tomorrow(), theme, reservationTime);
        Reservation second = ReservationFixture.createWithAll("아루", TestDateTimes.tomorrow(), theme, reservationTime);
        reservationRepository.save(first);

        // when & then: DB의 UK Constraint 발생 후 비즈니스 예외로 변환
        assertThatThrownBy(() -> reservationRepository.save(second))
                .isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void 존재하는_ID로_예약을_조회하면_엔트리와_함께_반환된다() {
        // given
        Reservation reservation = ReservationFixture.createWithAll("이프", TestDateTimes.tomorrow(), theme,
                reservationTime);
        Reservation saved = reservationRepository.save(reservation);

        // when:
        Optional<Reservation> find = reservationRepository.findById(saved.getId());

        // then
        assertThat(find).isPresent();
        assertThat(find.get().getEntries())
                .singleElement()
                .extracting(ReservationEntry::getId, ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly(1L, "이프", ReservationStatus.RESERVED);
    }

    @Test
    void 존재하지_않는_ID로_예약을_조회하면_빈_Optional이_반환된다() {
        // when:
        Optional<Reservation> find = reservationRepository.findById(1L);

        // then
        assertThat(find).isEmpty();
    }

    @Test
    void 예약_슬롯을_수정한다() {
        // given
        Reservation saved = reservationRepository.save(
                ReservationFixture.createWithAll("이프", TestDateTimes.tomorrow(), theme, reservationTime)
        );

        Reservation updated = Reservation.restore(
                saved.getId(),
                TestDateTimes.daysLater(2),
                theme,
                reservationTime,
                List.of()
        );

        // when
        reservationRepository.update(updated);

        // then
        Optional<Reservation> find = reservationRepository.findById(saved.getId());

        assertThat(find).isPresent();
        assertThat(find.get().getDate()).isEqualTo(TestDateTimes.daysLater(2));
        assertThat(find.get().getEntries())
                .singleElement()
                .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

    @Test
    void 예약_엔트리_상태를_수정한다() {
        // given
        Reservation saved = reservationRepository.save(
                ReservationFixture.createWithAll("이프", TestDateTimes.tomorrow(), theme, reservationTime)
        );

        // when
        saved.cancelEntry(reservedEntryId(saved));
        reservationRepository.save(saved);

        // then
        Reservation find = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(find.getEntries())
                .singleElement()
                .extracting(ReservationEntry::getStatus)
                .isEqualTo(ReservationStatus.DELETED);
    }

    @Test
    void 날짜_테마_시간으로_예약을_조회하면_엔트리와_함께_반환된다() {
        // given
        Reservation saved = reservationRepository.save(
                ReservationFixture.createWithAll("이프", TestDateTimes.tomorrow(), theme, reservationTime)
        );
        ReservationCondition condition = new ReservationCondition(saved.getDate(), theme.getId(),
                reservationTime.getId());

        // when
        Optional<Reservation> find = reservationRepository.findByDateAndThemeAndTimeForUpdate(condition);

        // then
        assertThat(find).isPresent();
        assertThat(find.get().getEntries())
                .singleElement()
                .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

    @Test
    void 예약_엔트리_ID로_예약을_조회하면_해당_예약의_모든_엔트리를_반환한다() {
        // given
        Reservation saved = reservationRepository.save(
                ReservationFixture.createWithAll("이프", TestDateTimes.tomorrow(), theme, reservationTime)
        );

        // when
        Optional<Reservation> find = reservationRepository.findByEntryIdForUpdate(reservedEntryId(saved));

        // then
        assertThat(find).isPresent();
        assertThat(find.get().getId()).isEqualTo(saved.getId());
        assertThat(find.get().getEntries())
                .singleElement()
                .extracting(ReservationEntry::getReserverName, ReservationEntry::getStatus)
                .containsExactly("이프", ReservationStatus.RESERVED);
    }

    private long reservedEntryId(Reservation reservation) {
        return reservation.getEntries()
                .stream()
                .filter(ReservationEntry::isReserved)
                .findFirst()
                .orElseThrow()
                .getId();
    }
}
