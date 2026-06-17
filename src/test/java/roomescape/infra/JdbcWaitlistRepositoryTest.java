package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@DataJpaTest
@Import({
    JdbcWaitlistRepository.class
})
class JdbcWaitlistRepositoryTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Test
    void 같은_슬롯의_대기_목록을_조회한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        Slot slot = slotRepository.getOrCreate(Slot.of(FUTURE_SECOND_DATE, reservationTime, theme));

        Long brieId = waitlistRepository.save(new Reservation("브리", slot), CREATED_AT);
        Long pobiId = waitlistRepository.save(new Reservation("포비", slot), CREATED_AT);
        Long neoId = waitlistRepository.save(new Reservation("네오", slot), CREATED_AT);

        List<Waitlist> waitlists = waitlistRepository.findBySlotId(slot.getId());

        assertThat(waitlists)
            .extracting(Waitlist::getId)
            .containsExactly(brieId, pobiId, neoId);
    }

    @Test
    void 여러_슬롯의_대기_목록을_한_번에_조회한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        Slot firstSlot = slotRepository.getOrCreate(Slot.of(FUTURE_SECOND_DATE, reservationTime, theme));
        Slot secondSlot = slotRepository.getOrCreate(Slot.of(FUTURE_SECOND_DATE.plusDays(1), reservationTime, theme));

        Long brieId = waitlistRepository.save(new Reservation("브리", firstSlot), CREATED_AT);
        Long pobiId = waitlistRepository.save(new Reservation("포비", firstSlot), CREATED_AT.plusMinutes(1));
        Long neoId = waitlistRepository.save(new Reservation("네오", secondSlot), CREATED_AT);

        List<Waitlist> waitlists = waitlistRepository.findBySlotIds(List.of(firstSlot.getId(), secondSlot.getId()));

        assertThat(waitlists)
            .extracting(Waitlist::getId)
            .containsExactly(brieId, pobiId, neoId);
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime).getId();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme).getId();
        return new Theme(
            id,
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnailImageUrl()
        );
    }

}
