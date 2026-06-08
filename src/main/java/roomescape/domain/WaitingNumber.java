package roomescape.domain;

public record WaitingNumber(int value) {

    private static final int FIRST_WAITING_NUMBER = 1;

    public WaitingNumber {
        if (value < FIRST_WAITING_NUMBER) {
            throw new IllegalArgumentException("대기 순번은 1 이상이어야 합니다.");
        }
    }

    public static WaitingNumber fromIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("대기 인덱스는 0 이상이어야 합니다.");
        }
        return new WaitingNumber(index + FIRST_WAITING_NUMBER);
    }
}
