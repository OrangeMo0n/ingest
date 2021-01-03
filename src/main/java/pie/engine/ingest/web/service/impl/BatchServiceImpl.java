package pie.engine.ingest.web.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pie.engine.ingest.web.domain.BatchInfo;
import pie.engine.ingest.web.domain.TaskInfo;
import pie.engine.ingest.web.service.IBatchService;
import pie.engine.ingest.web.utils.AsyncQueryBatch;
import pie.engine.ingest.web.utils.IngestEtcdTool;

@Service
public class BatchServiceImpl implements IBatchService {
    @Autowired
    private IngestEtcdTool ingestEtcdTool;

    @Autowired
    private AsyncQueryBatch asyncQueryBatch;

    @Override

    public List<String> getDataTypeList() {
        List<String> dataTypeList = Arrays.asList("landsat8_toa", "landsat7_toa", "sentinel1_grd", "sentinel2_l1c",
                "landsat8_sr");

        return dataTypeList;
    }

    @Override
    public List<BatchInfo> getBatchList(String dataType, String status) {
        List<BatchInfo> batchInfoList = new ArrayList<>();

        List<String> batchIdList = ingestEtcdTool.getBatchIds(dataType);

        List<Future<BatchInfo>> queryBatchList = new ArrayList<>();
        try {
            for (int i = 0; i < batchIdList.size(); i++) {
                Future<BatchInfo> future = asyncQueryBatch.queryBatch(dataType, batchIdList.get(i));
                queryBatchList.add(future);
            }

            for (Future future : queryBatchList) {
                BatchInfo curBatch = (BatchInfo) future.get();
                if (status != null && status.length() > 0) {
                    if (curBatch.getStatus().equals(status) == false) {
                        continue;
                    }
                }
                batchInfoList.add(curBatch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return batchInfoList;
    }

    @Override
    public BatchInfo getBatch(String dataType, String batchId) {
        BatchInfo batchInfo = ingestEtcdTool.getBatch(dataType, batchId, false);
        if (batchInfo == null) {
            return null;
        }

        return batchInfo;
    }

    @Override
    public List<TaskInfo> getTaskList(String dataType, String batchId) {
        List<TaskInfo> taskInfoList = new ArrayList<>();

        List<String> taskIdList = ingestEtcdTool.getTaskList(dataType, batchId);
        if (taskIdList == null || taskIdList.size() == 0) {
            return null;
        }

        for (String taskId : taskIdList) {
            TaskInfo curTaskInfo = ingestEtcdTool.getTask(dataType, taskId);
            if (curTaskInfo == null) {
                continue;
            }

            String taskState = curTaskInfo.getState();
            String taskLock = curTaskInfo.getLock();

            if (taskState.equals("download_doing") || taskState.equals("prepro_doing")
                    || taskState.equals("upload_doing")) {
                if (taskLock == null || taskLock.length() == 0) {
                    curTaskInfo.setException(true);
                }
            }

            if (taskState.equals("download_fail") || taskState.equals("prepro_fail")
                    || taskState.equals("upload_fail")) {
                curTaskInfo.setException(true);
            }
            taskInfoList.add(curTaskInfo);
        }

        return taskInfoList;
    }
}