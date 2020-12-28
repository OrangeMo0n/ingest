package pie.engine.ingest.web.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pie.engine.ingest.web.domain.BatchInfo;
import pie.engine.ingest.web.domain.TaskInfo;
import pie.engine.ingest.web.service.IBatchService;
import pie.engine.ingest.web.utils.IngestEtcdTool;

@Service
public class BatchServiceImpl implements IBatchService
{
    @Autowired
    private IngestEtcdTool ingestEtcdTool;

    @Override
    public List<String> getDataTypeList()
    {
        List<String> dataTypeList = 
            Arrays.asList("landsat8_toa", 
                "landsat7_toa",
                "sentinel1_grd",
                "sentinel2_l1c",
                "landsat8_sr");
        
        return dataTypeList;
    }
    
    @Override
    public List<BatchInfo> getBatchList(String dataType, String status)
    {
        List<BatchInfo> batchInfoList = new ArrayList<>();
        
        List<String> batchIdList = ingestEtcdTool.getBatchIds(dataType);

        for (String curBatchId : batchIdList) {
            BatchInfo curBatchInfo = ingestEtcdTool.getBatch(dataType, curBatchId, true);
            if (curBatchInfo != null) {
                List<String> taskIdList = ingestEtcdTool.getTaskList(dataType, curBatchId);
                Integer allCount = taskIdList.size();
                curBatchInfo.setAllCount(allCount);
                Integer initCount = 0, runningCount = 0, successCount = 0, failedCount = 0, confirmedCount = 0;
                for (int i=0; i<allCount; i++) {
                    String curTaskId = taskIdList.get(i);

                    TaskInfo curTask = ingestEtcdTool.getTask(dataType, curTaskId);
                    if (curTask == null) {
                        continue;
                    }
                    String taskState = curTask.getState();
                    if (taskState.equals("init") || taskState.equals("download_success") ||
                        taskState.equals("prepro_success")) {
                        initCount ++;
                    }
                    else if (taskState.equals("upload_success")) {
                        successCount ++;
                    }
                    else if (taskState.equals("confirmed")) {
                        confirmedCount ++;
                    }
                    else if (taskState.equals("download_fail") || taskState.equals("prepro_fail") ||
                        taskState.equals("prepro_fail")) {
                        failedCount ++;
                    }
                    else {
                        String taskLock = curTask.getLock();
                        if (taskLock == null || taskLock.length() == 0) {
                            failedCount ++;
                        }
                        else {
                            runningCount ++;
                        }
                    }
                }

                curBatchInfo.setInitCount(initCount);
                curBatchInfo.setRunningCount(runningCount);
                curBatchInfo.setSuccessCount(successCount);
                curBatchInfo.setFailedCount(failedCount);
                curBatchInfo.setConfirmedCount(confirmedCount);
                
                if (allCount == initCount) {
                    curBatchInfo.setStatus("INITIAL");
                }
                else if (allCount == confirmedCount) {
                    curBatchInfo.setStatus("CONFIRMED");
                }
                else if (allCount == (successCount+confirmedCount)) {
                    curBatchInfo.setStatus("SUCCESS");
                }
                else if (failedCount > 0) {
                    curBatchInfo.setStatus("ABORTED");
                }
                else if (runningCount > 0) {
                    curBatchInfo.setStatus("RUNNING");
                }

                batchInfoList.add(curBatchInfo);
            }
        }

        return batchInfoList;
    }

    @Override
    public BatchInfo getBatch(String batchId)
    {
        BatchInfo batchInfo = new BatchInfo();

        return batchInfo;
    }
}