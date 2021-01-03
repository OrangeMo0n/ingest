package pie.engine.ingest.web.utils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
//import pie.engine.ingest.web.config.EtcdConfig;
import pie.engine.ingest.web.domain.BatchInfo;
import pie.engine.ingest.web.domain.TaskInfo;

public class IngestEtcdTool {
    KV kvClient;
    Charset etcdCharset = Charset.forName("utf8");

    /*
     * public static IngestEtcdTool ingestEtcdTool = new IngestEtcdTool();
     * 
     * public IngestEtcdTool() { //EtcdConfig etcdConfig = new EtcdConfig();
     * 
     * //String etcd_addr = "http://" + etcdConfig.getEtcdIp() + ":" +
     * etcdConfig.getEtcdPort(); String etcd_addr = "http://127.0.0.1:2379"; try {
     * Client client = Client.builder().endpoints(etcd_addr).build(); kvClient =
     * client.getKVClient(); } catch (Exception e) { e.printStackTrace(); } }
     */

    public boolean connectToEtcd(String ip, Integer port) {
        String etcd_addr = "http://" + ip + ":" + port;
        try {
            Client client = Client.builder().endpoints(etcd_addr).build();
            kvClient = client.getKVClient();
        } catch (Exception e) {
            System.out.println("Catch exception when connect to etcd!");
            return false;
        }

        return true;
    }

    public List<String> getPrefixKeyOnly(String keyPrefix) {
        List<String> kMap = new ArrayList<>();

        try {
            ByteSequence keyPrefixByteSeq = ByteSequence.from(keyPrefix.getBytes());
            ByteSequence endKeyByteSeq = ByteSequence.from((keyPrefix + "0").getBytes());
            CompletableFuture<GetResponse> getFuture = kvClient.get(keyPrefixByteSeq,
                    GetOption.newBuilder().withKeysOnly(true).withRange(endKeyByteSeq).build());
            GetResponse getResponse = getFuture.get();
            int kvCount = getResponse.getKvs().size();
            for (int i = 0; i < kvCount; i++) {
                String kvKey = getResponse.getKvs().get(i).getKey().toString(etcdCharset);
                kMap.add(kvKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return kMap;
    }

    public Map<String, String> getPrefix(String keyPrefix) {
        Map<String, String> kvMap = new HashMap<String, String>();
        try {
            ByteSequence keyPrefixByteSeq = ByteSequence.from(keyPrefix.getBytes());
            ByteSequence endKeyByteSeq = ByteSequence.from((keyPrefix + "0").getBytes());
            CompletableFuture<GetResponse> getFuture = kvClient.get(keyPrefixByteSeq,
                    GetOption.newBuilder().withRange(endKeyByteSeq).build());
            GetResponse getResponse = getFuture.get();
            int kvCount = getResponse.getKvs().size();
            for (int i = 0; i < kvCount; i++) {
                String kvKey = getResponse.getKvs().get(i).getKey().toString(etcdCharset);
                String kvValue = getResponse.getKvs().get(i).getValue().toString(etcdCharset);
                kvMap.put(kvKey, kvValue);
            }
        } catch (Exception e) {
            System.out.println("Catch exception when get etcd record by prefix!");
            return null;
        }
        return kvMap;
    }

    public String get(String key) {
        String value = "";
        try {
            ByteSequence keyByteSeq = ByteSequence.from(key.getBytes());
            CompletableFuture<GetResponse> getFuture = kvClient.get(keyByteSeq);
            GetResponse getResponse = getFuture.get();
            int kvCount = getResponse.getKvs().size();
            if (kvCount != 1) {
                System.out.println("Get more than one record by key!key:" + key);
                return "";
            }
            value = getResponse.getKvs().get(0).getValue().toString(etcdCharset);
        } catch (Exception e) {
            System.out.println("Catch exception when get etcd record by key!");
            return "";
        }
        return value;
    }

    public boolean put(String key, String value) {
        try {
            ByteSequence keyByteSeq = ByteSequence.from(key.getBytes());
            ByteSequence valueByteSeq = ByteSequence.from(value.getBytes());

            kvClient.put(keyByteSeq, valueByteSeq);
        } catch (Exception e) {
            System.out.println("Catch exception when put etcd record by key:" + key + ", value:" + value);
            return false;
        }

        return true;
    }

    public String getBatchAttr(String typeId, String batchId, String key) {
        String fullKeyNode = "/ingest/" + typeId + "/batch/" + batchId + "/" + key;

        try {
            return get(fullKeyNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean putBatchAttr(String typeId, String batchId, String key, String value) {
        try {
            String fullKeyNode = "/ingest/" + typeId + "/batch/" + batchId + "/" + key;
            return put(fullKeyNode, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTaskAttr(String typeId, String taskId, String key) {
        String fullKeyNode = "/ingest/" + typeId + "/task/" + taskId + "/" + key;

        try {
            return get(fullKeyNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean putTaskAttr(String typeId, String taskId, String key, String value) {
        try {
            String fullKeyNode = "/ingest/" + typeId + "/task/" + taskId + "/" + key;
            return put(fullKeyNode, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getTaskList(String typeId, String batchId) {
        List<String> taskIdList = new ArrayList<>();

        try {
            String etcdNodeKey = "/ingest/" + typeId + "/batch/" + batchId + "/task_list";

            String taskListString = get(etcdNodeKey);
            String[] taskListArray = taskListString.split(",");
            for (int j = 0; j < taskListArray.length; j++) {
                taskIdList.add(taskListArray[j]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return taskIdList;
    }

    public BatchInfo getBatch(String typeId, String batchId, boolean noTaskList) {
        BatchInfo batchInfo = null;
        try {
            String etcd_node_prefix = "/ingest/" + typeId + "/batch/" + batchId;

            Map<String, String> batchKvMap = getPrefix(etcd_node_prefix);
            if (batchKvMap.size() <= 0) {
                return null;
            }

            batchInfo = new BatchInfo();
            batchInfo.setBatchID(batchId);

            for (Map.Entry<String, String> entry : batchKvMap.entrySet()) {
                String mapKey = entry.getKey();
                mapKey = mapKey.replace(etcd_node_prefix + "/", "");
                switch (mapKey) {
                    case "create_time":
                        batchInfo.setCreateBatchTime(entry.getValue());
                        break;
                    case "create_param":
                        batchInfo.setCreateParam(entry.getValue());
                        break;
                    case "data_source_type":
                        batchInfo.setDataSourceType(entry.getValue());
                        break;
                    case "status":
                        batchInfo.setStatus(entry.getValue());
                        break;
                    case "tag":
                        batchInfo.setBatchTag(entry.getValue());
                        break;
                    case "start_pro_time":
                        batchInfo.setStartProTime(entry.getValue());
                        break;
                    case "end_pro_time":
                        batchInfo.setEndProTime(entry.getValue());
                        break;
                    case "task_list":
                        if (!noTaskList) {
                            String taskList = entry.getValue();
                            String[] taskListArray = taskList.split(",");
                            List<String> taskIdList = new ArrayList<>();
                            for (int j = 0; j < taskListArray.length; j++) {
                                taskIdList.add(taskListArray[j]);
                            }
                            batchInfo.setTaskList(taskIdList);
                        }

                        break;
                    default:
                        batchInfo.addOtherInfo(mapKey, entry.getValue());
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Catch exception when get etcd batch by batch id:" + batchId);
            return null;
        }

        return batchInfo;
    }

    public TaskInfo getTask(String typeId, String taskId) {
        TaskInfo taskInfo = null;
        try {
            String etcd_node_prefix = "/ingest/" + typeId + "/task/" + taskId;
            Map<String, String> taskKvMap = getPrefix(etcd_node_prefix);
            if (taskKvMap.size() <= 0) {
                return null;
            }

            taskInfo = new TaskInfo();
            taskInfo.setTaskID(taskId);
            taskInfo.setSourceDataType(typeId);
            for (Map.Entry<String, String> entry : taskKvMap.entrySet()) {
                String mapKey = entry.getKey();
                mapKey = mapKey.replace(etcd_node_prefix + "/", "");
                switch (mapKey) {
                    case "state":
                        taskInfo.setState(entry.getValue());
                        break;
                    case "substate":
                        taskInfo.setSubState(entry.getValue());
                        break;
                    case "lock":
                        taskInfo.setLock(entry.getValue());
                        break;
                    case "start_pro_time":
                        taskInfo.setStartProTime(entry.getValue());
                        break;
                    case "end_pro_time":
                        taskInfo.setEndTime(entry.getValue());
                        break;
                    default:
                        taskInfo.addOtherInfo(mapKey, entry.getValue());
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Catch exception when get etcd task by task id:" + taskId);
            return null;
        }

        return taskInfo;
    }

    public List<String> getBatchIds(String typeId) {
        List<String> batchIdList = new ArrayList<>();

        try {
            String etcd_node_prefix = "/ingest/" + typeId + "/batch";
            List<String> batchKMap = getPrefixKeyOnly(etcd_node_prefix);

            if (batchKMap.size() <= 0) {
                return null;
            }

            for (String batchKey : batchKMap) {
                batchKey = batchKey.replace(etcd_node_prefix + "/", "");
                String[] splitKeys = batchKey.split("/");
                if (splitKeys.length <= 0) {
                    continue;
                }
                String tempBatchId = splitKeys[0];
                if (tempBatchId.length() != 16) {
                    continue;
                }

                boolean existBatch = false;
                for (int j = 0; j < batchIdList.size(); j++) {
                    if (batchIdList.get(j).equals(tempBatchId)) {
                        existBatch = true;
                        break;
                    }
                }
                if (existBatch) {
                    continue;
                }
                batchIdList.add(tempBatchId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Catch exception when get etcd batch ids，type id:" + typeId);
            return null;
        }

        return batchIdList;
    }

    public List<String> getTaskIds(String typeId) {
        List<String> taskIdList = new ArrayList<>();

        try {
            String etcd_node_prefix = "/ingest/" + typeId + "/task";
            List<String> taskKMap = getPrefixKeyOnly(etcd_node_prefix);

            if (taskKMap.size() <= 0) {
                return null;
            }

            for (String taskKey : taskKMap) {
                taskKey = taskKey.replace(etcd_node_prefix + "/", "");
                String[] splitKeys = taskKey.split("/");
                if (splitKeys.length <= 0) {
                    continue;
                }
                String tempTaskId = splitKeys[0];

                boolean existTask = false;
                for (int j = 0; j < taskIdList.size(); j++) {
                    if (taskIdList.get(j).equals(tempTaskId)) {
                        existTask = true;
                        break;
                    }
                }
                if (existTask) {
                    continue;
                }
                taskIdList.add(tempTaskId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Catch exception when get etcd batch ids，type id:" + typeId);
            return null;
        }

        return taskIdList;
    }

}