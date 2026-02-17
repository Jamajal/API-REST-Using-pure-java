package TrainningRoom.HTTP;

public enum CodeStatus {
    OK(200, "OK"), 
    BAD_REQUEST(400, "BAD REQUEST"),
    NOT_FOUND(404, "NOT FOUND");

    private final int value;
    private final String title;

    CodeStatus(int optionValue, String titleValue) {
        value = optionValue;
        title = titleValue;
    }

    public String getStatusPrint() {
        return value + " " + title;
    }
}
