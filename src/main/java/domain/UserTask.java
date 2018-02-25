package domain;

public class UserTask {

    private String id;
    private String elementName;
    private String enterScript;
    private String exitScript;
    private String localStepName;
    private String generalStepName;
    private String stepRefId;

    public UserTask(String id, String elementName, String enterScript, String exitScript) {
        this.id = id;
        this.elementName = elementName;
        this.enterScript = enterScript;
        this.exitScript = exitScript;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getEnterScript() {
        return enterScript;
    }

    public void setEnterScript(String enterScript) {
        this.enterScript = enterScript;
    }

    public String getExitScript() {
        return exitScript;
    }

    public void setExitScript(String exitScript) {
        this.exitScript = exitScript;
    }

    public String getLocalStepName() {
        return localStepName;
    }

    public void setLocalStepName(String localStepName) {
        this.localStepName = localStepName;
    }

    public String getStepRefId() {
        return stepRefId;
    }

    public void setStepRefId(String stepRefId) {
        this.stepRefId = stepRefId;
    }

    public String getGeneralStepName() {
        return generalStepName;
    }

    public void setGeneralStepName(String generalStepName) {
        this.generalStepName = generalStepName;
    }

    @Override
    public String toString() {
        return "UserTask{" +
                "id='" + id + '\'' +
                ", elementName='" + elementName + '\'' +
                ", localStepName='" + localStepName + '\'' +
                ", stepRefId='" + stepRefId + '\'' +
                '}';
    }
}
