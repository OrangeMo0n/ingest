package pie.engine.ingest.web.service;

import java.util.List;

import pie.engine.ingest.web.domain.BatchInfo;
import pie.engine.ingest.web.domain.TaskInfo;

public interface IBatchService {
    /**
     * 获取所有的数据类型
     * 
     * @return
     */
    public List<String> getDataTypeList();

    /**
     * 根据数据类型获取batch信息列表
     * 
     * @param dataType
     * @return
     */
    public List<BatchInfo> getBatchList(String dataType, String status);

    /**
     * 根据batch ID获取batch信息
     * 
     * @param batchId
     * @return
     */
    public BatchInfo getBatch(String dataType, String batchId);

    /**
     * 根据数据类型和batch id获取所有task信息
     * 
     * @param dataType
     * @param batchId
     * @return
     */
    public List<TaskInfo> getTaskList(String dataType, String batchId);
}