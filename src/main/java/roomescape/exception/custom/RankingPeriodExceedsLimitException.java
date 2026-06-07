package roomescape.exception.custom;

public class RankingPeriodExceedsLimitException extends CustomException {

    public RankingPeriodExceedsLimitException() {
        super("랭킹 조회 기간이 최대 기간을 초과했습니다.");
    }
}
