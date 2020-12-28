package pie.engine.ingest.web.domain;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import pie.engine.ingest.framework.core.domain.BaseEntity;

public class TaskInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "Task id")
    private String taskID;

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getTaskID() {
        return taskID;
    }

    @ApiModelProperty(name = "DataType数据类型")
    private String sourceDataType;

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    @ApiModelProperty(name = "Task State状态")
    private String state;

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    @ApiModelProperty(name = "Task Substate子状态")
    private String subState;

    public void setSubState(String subState) {
        this.subState = subState;
    }

    public String getSubState() {
        return subState;
    }

    @ApiModelProperty(name = "Task lock处理中")
    private String lock;

    public void setLock(String lock) {
        this.lock = lock;
    }

    public String getLock() {
        return lock;
    }

    @ApiModelProperty(name = "开始处理时间")
    private String startProTime;

    public void setStartProTime(String startProTime) {
        this.startProTime = startProTime;
    }

    public String getStartProTime() {
        return startProTime;
    }

    @ApiModelProperty(name = "结束处理时间")
    private String endProTime;

    public void setEndProTime(String endProTime) {
        this.endProTime = endProTime;
    }

    public String getEndProTime() {
        return endProTime;
    }
    
    @ApiModelProperty(name = "其他信息")
    private Map<String, String> otherTaskInfo = new HashMap<String, String>();

    public void addOtherInfo(String key, String value) {
        otherTaskInfo.put(key, value);
    }

    public Map<String, String> getOtherInfo() {
        return otherTaskInfo;
    }
}