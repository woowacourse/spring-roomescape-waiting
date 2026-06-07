package roomescape.exception.custom;

public class RankingPeriodPastDateOnlyException extends CustomException {

    public RankingPeriodPastDateOnlyException() {
        super("랭킹 조회는 오늘 날짜 이전까지만 가능합니다.");
    }
}
