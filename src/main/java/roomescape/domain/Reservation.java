package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.PastSessionControlException;
import roomescape.exception.PastTimeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final Session session;
    private final Long amount;

    public Reservation(Long id, String name, Session session, Long amount) {
        validate(name, session);
        this.id = id;
        this.name = name;
        this.session = session;
        this.amount = amount;
    }

    public static Reservation transientOf(String name, Session session, Long amount) {
        return new Reservation(null, name, session, amount);
    }

    public Reservation reschedule(Session session, LocalDateTime currentDateTime) {
        Session patchedSession = Objects.requireNonNullElse(session, this.session);
        validateNotPast(currentDateTime);
        return new Reservation(this.id, this.name, patchedSession, this.amount);
    }

    public boolean isReservedBy(String name) {
        return this.name.equals(name);
    }

    public void validateModifiable(String requesterName, LocalDateTime currentDateTime) {
        if (!this.name.equals(requesterName)) {
            throw new InvalidOwnershipException();
        }
        if (this.session.isPast(currentDateTime)) {
            throw new PastSessionControlException();
        }
    }

    public void validateNotPast(LocalDateTime currentDateTime) {
        if (this.session.isPast(currentDateTime)) {
            throw new PastTimeException("지난 시간/날짜로 예약하실 수 없습니다.");
        }
    }

    private void validate(String name, Session session) {
        if (name == null || name.isBlank() || session == null) {
            throw new IllegalArgumentException("필수 예약 정보가 누락되었습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Session getSession() {
        return session;
    }

    public Long getAmount() {
        return amount;
    }
}
