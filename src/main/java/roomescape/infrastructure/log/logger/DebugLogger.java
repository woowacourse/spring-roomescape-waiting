package roomescape.infrastructure.log.logger;

public class DebugLogger implements RoomEscapeLog {

    @Override
    public void printLog(String message) {
        System.out.println("[LOG]" + message);
    }
}
