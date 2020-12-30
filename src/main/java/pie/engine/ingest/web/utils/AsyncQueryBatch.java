package pie.engine.ingest.web.utils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import pie.engine.ingest.web.domain.BatchInfo;
import pie.engine.ingest.web.domain.TaskInfo;

@Component
public class AsyncQueryBatch {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IngestEtcdTool etcdTool;

    @Async("batchQueryAsyncPool")
    public Future<BatchInfo> queryBatch(String typeId, String batchId/* , CountDownLatch latch */) {
        logger.info("Query batch:" + batchId);
        BatchInfo curBatchInfo = etcdTool.getBatch(typeId, batchId, true);

        if (curBatchInfo == null) {
            return null;
        }

        List<String> taskList = etcdTool.getTaskList(typeId, batchId);
        Integer allCount = taskList.size();
        Integer initCount = 0, runningCount = 0, successCount = 0, failedCount = 0, confirmedCount = 0;
        for (int i = 0; i < allCount; i++) {
            String curTaskId = taskList.get(i);

            TaskInfo curTask = etcdTool.getTask(typeId, curTaskId);
            if (curTask == null) {
                continue;
            }
            String taskState = curTask.getState();
            if (taskState.equals("init") || taskState.equals("download_success")
                    || taskState.equals("prepro_success")) {
                initCount++;
            } else if (taskState.equals("upload_success")) {
                successCount++;
            } else if (taskState.equals("confirmed")) {
                confirmedCount++;
            } else if (taskState.equals("download_fail") || taskState.equals("prepro_fail")
                    || taskState.equals("prepro_fail")) {
                failedCount++;
            } else {
                String taskLock = curTask.getLock();
                if (taskLock == null || taskLock.length() == 0) {
                    failedCount++;
                } else {
                    runningCount++;
                }
            }
        }

        curBatchInfo.setAllCount(allCount);
        curBatchInfo.setInitCount(initCount);
        curBatchInfo.setRunningCount(runningCount);
        curBatchInfo.setSuccessCount(successCount);
        curBatchInfo.setFailedCount(failedCount);
        curBatchInfo.setConfirmedCount(confirmedCount);

        if (allCount.equals(initCount)) {
            curBatchInfo.setStatus("INITIAL");
        } else {
            if (allCount.equals(confirmedCount)) {
                curBatchInfo.setStatus("CONFIRMED");
            } else {
                if (allCount.equals(successCount + confirmedCount)) {
                    curBatchInfo.setStatus("SUCCESS");
                } else {
                    if (failedCount.intValue() > 0) {
                        curBatchInfo.setStatus("ABORTED");
                    } else {
                        curBatchInfo.setStatus("RUNNING");
                    }
                }
            }
        }

        logger.info(batchId + ": Status-" + curBatchInfo.getStatus() + ", initCount-" + initCount + ", runningCount-"
                + runningCount + ", successCount-" + successCount + ", failedCount-" + failedCount + ", confirmedCount-"
                + confirmedCount);

        // latch.countDown();

        return new AsyncResult<>(curBatchInfo);
    }
}
