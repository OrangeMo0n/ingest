package pie.engine.ingest.web.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import pie.engine.ingest.framework.core.domain.BaseEntity;

public class BatchInfo extends BaseEntity 
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "batch的16位随机ID号")
    private String batchID;

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public String getBatchID() {
        return batchID; 
    }

    @ApiModelProperty(name = "batch的数据类型")
    private String dataSourceType;

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    @ApiModelProperty(name = "任务状态")
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @ApiModelProperty(name = "tag标记别称")
    private String batchTag;

    public void setBatchTag(String batchTag) {
        this.batchTag = batchTag;
    }

    public String getBatchTag() {
        return batchTag;
    }

    @ApiModelProperty(name = "Batch创建参数")
    private String createParam;

    public void setCreateParam(String createParam) {
        this.createParam = createParam;
    }

    public String getCreateParam() {
        return createParam;
    }

    @ApiModelProperty(name = "创建时间")
    private String createBatchTime;

    public void setCreateBatchTime(String createBatchTime) {
        this.createBatchTime = createBatchTime;
    }

    public String getCreateBatchTime() {
        return createBatchTime;
    }

    @ApiModelProperty(name = "开始时间")
    private String startProTime;

    public void setStartProTime(String startProTime) {
        this.startProTime = startProTime;
    }

    public String getStartProTime() {
        return startProTime;
    }

    @ApiModelProperty(name = "结束时间")
    private String endProTime;

    public void setEndProTime(String endProTime) {
        this.endProTime = endProTime;
    }

    public String getEndProTime() {
        return endProTime;
    }

    @ApiModelProperty(name = "task列表")
    private List<String> taskList;

    public void setTaskList(List<String> taskList) {
        this.taskList = taskList;
    }

    public List<String> getTaskList() {
        return taskList;
    }

    @ApiModelProperty(name = "task任务总数")
    private Integer allCount;

    public void setAllCount(Integer allCount) {
        this.allCount = allCount;
    }

    public Integer getAllCount() {
        return allCount;
    }

    @ApiModelProperty(name = "task初始化总数")
    private Integer initCount;

    public void setInitCount(Integer initCount) {
        this.initCount = initCount;
    }

    public Integer getInitCount() {
        return initCount;
    }

    @ApiModelProperty(name = "task进行总数")
    private Integer runningCount;

    public void setRunningCount(Integer runningCount) {
        this.runningCount = runningCount;
    }

    public Integer getRunningCount() {
        return runningCount;
    }

    @ApiModelProperty(name = "task成功总数")
    private Integer successCount;

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    @ApiModelProperty(name = "task失败总数")
    private Integer failedCount;

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    @ApiModelProperty(name = "task confirm总数")
    private Integer confirmedCount;

    public void setConfirmedCount(Integer confirmedCount) {
        this.confirmedCount = confirmedCount;
    }

    public Integer getConfirmedCount() {
        return confirmedCount;
    }

    @ApiModelProperty(name = "其他信息")
    private Map<String, String> otherBatchInfo = new HashMap<String, String>();

    public void addOtherInfo(String key, String value) {
        otherBatchInfo.put(key, value);
    }

    public Map<String, String> getOtherInfo() {
        return otherBatchInfo;
    }

}
