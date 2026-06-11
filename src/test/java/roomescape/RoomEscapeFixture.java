package roomescape;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.controller.dto.request.ThemeFamousFindRequest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

public class RoomEscapeFixture {
    static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-05-10T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );
    public static final LocalDateTime PAST_DATE_TIME = LocalDateTime.of(2000, 11, 11, 10, 0);
    public static final LocalDateTime FUTURE_DATE_TIME = LocalDateTime.of(2099, 11, 11, 10, 0);

    public static final ReservationName NAME = new ReservationName("zeze");
    public static final ReservationDate FUTURE_DATE = new ReservationDate(FUTURE_DATE_TIME.toLocalDate());
    public static final ReservationDate PAST_DATE = new ReservationDate(PAST_DATE_TIME.toLocalDate());
    public static final ReservationTime TIME = ReservationTime.of(LocalTime.of(10, 0));
    public static final Theme THEME = Theme.create(new ThemeName("공포"), "무서워요", new ThumbnailUrl("https://zeze.com"));

    public static SlotBuilder slot() {
        return new SlotBuilder();
    }

    public static ReservationBuilder reservation() {
        return new ReservationBuilder();
    }

    public static Theme theme() {
        return THEME;
    }

    public static ThemeFamousFindRequest themeFamousFindRequest() {
        return new ThemeFamousFindRequest(7L, FUTURE_DATE.getValue(), 10L);
    }

    public static ReservationCreateRequest reservationCreateRequestWithName(ReservationName name) {
        return new ReservationCreateRequest(name.getValue(), FUTURE_DATE.getValue(), 1L, 1L);
    }

    public static ReservationUpdateRequest reservationUpdateRequest() {
        return new ReservationUpdateRequest(NAME.getValue(), FUTURE_DATE.getValue(), 1L, 1L);
    }

    public static class SlotBuilder {
        private long id = 1L;
        private ReservationDate date = FUTURE_DATE;
        private ReservationTime time = TIME;
        private Theme theme = THEME;

        public SlotBuilder id(long id) {
            this.id = id;
            return this;
        }

        public SlotBuilder date(ReservationDate date) {
            this.date = date;
            return this;
        }

        public SlotBuilder time(ReservationTime time) {
            this.time = time;
            return this;
        }

        public SlotBuilder theme(Theme theme) {
            this.theme = theme;
            return this;
        }

        public Slot build() {
            return Slot.load(id, date, time, theme);
        }
    }

    public static class ReservationBuilder {
        private long id = 1L;
        private ReservationName name = NAME;
        private Slot slot = RoomEscapeFixture.slot().build();
        private Status status = Status.APPROVED;
        private LocalDateTime createdAt = LocalDateTime.now(FIXED_CLOCK);

        public ReservationBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ReservationBuilder name(String name) {
            this.name = new ReservationName(name);
            return this;
        }

        public ReservationBuilder slot(Slot slot) {
            this.slot = slot;
            return this;
        }

        public ReservationBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public ReservationBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Reservation build() {
            return Reservation.load(id, name, slot, status, createdAt);
        }
    }
}
