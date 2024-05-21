package roomescape.reservation.domain;

import java.util.Objects;
import roomescape.global.exception.DomainValidationException;

public class WaitingStatus {

    private static final int RESERVED = 1;
    private static final int MAX_WAITING = 5;

    private int waitingNumber;

    protected WaitingStatus() {
    }

    public WaitingStatus(int waitingNumber) {
        validate(waitingNumber);
        this.waitingNumber = waitingNumber;
    }

    public static WaitingStatus createReservedStatus() {
        return new WaitingStatus(RESERVED);
    }

    private void validate(int waitingNumber) {
        validatePositive(waitingNumber);
        validateRange(waitingNumber);
    }

    private void validatePositive(int waitingNumber) {
        if (waitingNumber <= 0) {
            throw new DomainValidationException("예약 대기 번호는 0이거나 음수일 수 없습니다" + " 대기 번호: " + waitingNumber);
        }
    }

    private void validateRange(int waitingNumber) {
        if (waitingNumber > MAX_WAITING) {
            throw new DomainValidationException("예약 대기는 " + MAX_WAITING + "명까지 가능합니다 현재: " + MAX_WAITING + "명");
        }
    }

    public boolean isWaiting() {
        return waitingNumber != RESERVED;
    }

    public WaitingStatus rankUp() {
        return new WaitingStatus(waitingNumber - 1);
    }

    public int getWaitingNumber() {
        return waitingNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaitingStatus that = (WaitingStatus) o;
        return waitingNumber == that.waitingNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(waitingNumber);
    }
}
