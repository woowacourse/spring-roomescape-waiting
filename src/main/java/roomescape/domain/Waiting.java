package roomescape.domain;

import java.time.LocalDateTime;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.PastSessionControlException;
import roomescape.exception.PastTimeException;

public class Waiting implements Comparable<Waiting> {

    private final Long id;
    private final String name;
    private final Session session;
    private final Integer waitingNumber;

    public Waiting(Long id, String name, Session session, Integer waitingNumber) {
        validateFields(name, session);
        this.id = id;
        this.name = name;
        this.session = session;
        this.waitingNumber = waitingNumber;
    }

    public static Waiting transientOf(String name, Session session) {
        return new Waiting(null, name, session, null);
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
            throw new PastTimeException("지난 시간/날짜로 예약 대기를 추가하실 수 없습니다.");
        }
    }

    @Override
    public int compareTo(Waiting other) {
        return Integer.compare(this.waitingNumber, other.waitingNumber);
    }

    private void validateFields(String name, Session session) {
        if (name == null || name.isBlank() || session == null) {
            throw new IllegalArgumentException("필수 대기 정보가 누락되었습니다.");
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

    public Integer getWaitingNumber() {
        return waitingNumber;
    }
}
