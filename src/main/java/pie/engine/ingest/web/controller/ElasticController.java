package pie.engine.ingest.web.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import pie.engine.ingest.web.config.ClientConfiguration;
import pie.engine.ingest.web.utils.DataStatistics;
import pie.engine.ingest.web.utils.Hits;
import pie.engine.ingest.web.utils.Source;

@RestController
@RequestMapping("elastic")
@Api(tags = "elastic信息查询")
public class ElasticController {

    @Value("${ssh.url.total}")
    private String totalQueryUrl;

    @Value("${ssh.url.datasets}")
    private String datasetsQueryUrl;

    @Value("${ssh.user}")
    private String sshUser;

    @Value("${ssh.password}")
    private String sshPassword;

    @Autowired
    private ClientConfiguration con;

    public Map<String, Object> getAuth() {
        String enconding = DatatypeConverter
                .printBase64Binary(String.format("%s:%s", sshUser, sshPassword).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> header = new HashMap<>();
        header.put("Authorization", "Basic: " + enconding);
        return header;
    }

    private String getTotalMessage() throws IOException {
        Map<String, Object> auth = getAuth();
        CloseableHttpResponse sendGet = con.sendGet(totalQueryUrl, auth, null);
        HttpEntity entity = sendGet.getEntity();
        String result = EntityUtils.toString(entity, "UTF-8");
        return result;
    }

    private String getDate(int val) {
        Date date = new Date();// 取时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, val); // 把日期往后增加一天.整数往后推,负数往前移动
        date = calendar.getTime(); // 这个时间就是日期往后推一天的结果
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(date);
        return dateString;
    }

    private double getDouble(double d) {
        return (double) Math.round(d * 100) / 100;
    }

    @RequestMapping("/getIngestTotal")
    @ApiOperation("获取数据集入库总量信息")
    public Map<String, Object> getIngestTotal() throws ParseException, IOException {
        String mes = getTotalMessage();
        System.out.println(mes);
        Map<String, Object> map = new HashMap<String, Object>();

        JSONObject parseObject = JSON.parseObject(mes);
        JSONObject hits = parseObject.getJSONObject("hits");
        List<Hits> jsonArray = JSON.parseArray(hits.getJSONArray("hits").toString(), Hits.class);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String now = format.format(new Date());
        DataStatistics dataStatistics_now = new DataStatistics();
        DataStatistics dataStatistics = new DataStatistics();
        boolean flag = false;
        for (Hits data : jsonArray) {
            if (now.equals(data.get_id())) {
                Source get_source = data.get_source();
                dataStatistics_now.setCount(get_source.getCount());
                dataStatistics_now.setSize(get_source.getSize());
                dataStatistics_now.setDatasets(get_source.getDatasets().size());
                map.put("total", dataStatistics_now);
                map.put("datasets", get_source.getDatasets());
                flag = true;
            }
        }
        boolean flag2 = false;
        for (Hits data : jsonArray) {
            if (getDate(-1).equals(data.get_id())) {
                Source get_source = data.get_source();
                dataStatistics.setCount(get_source.getCount());
                dataStatistics.setSize(get_source.getSize());
                dataStatistics.setDatasets(get_source.getDatasets().size());
                if (!flag) {
                    map.put("total", dataStatistics);
                    map.put("datasets", get_source.getDatasets());
                }
                flag2 = true;
            }
        }
        if (flag2) {
            boolean yesflg = false;
            boolean weekflg = false;
            boolean monthflg = false;
            for (Hits data : jsonArray) {
                if (getDate(-2).equals(data.get_id())) {
                    DataStatistics dataStatistics2 = new DataStatistics();
                    Source get_source = data.get_source();
                    dataStatistics2.setCount(dataStatistics.getCount() - get_source.getCount());
                    dataStatistics2.setSize(getDouble(dataStatistics.getSize() - get_source.getSize()));
                    dataStatistics2.setDatasets(dataStatistics.getDatasets() - get_source.getDatasets().size());
                    map.put("daily", dataStatistics2);
                    yesflg = true;
                }
                if (getDate(-8).equals(data.get_id())) {
                    DataStatistics dataStatistics3 = new DataStatistics();
                    Source get_source = data.get_source();
                    dataStatistics3.setCount(dataStatistics.getCount() - get_source.getCount());
                    dataStatistics3.setSize(getDouble(dataStatistics.getSize() - get_source.getSize()));
                    dataStatistics3.setDatasets(dataStatistics.getDatasets() - get_source.getDatasets().size());
                    map.put("weekly", dataStatistics3);
                    weekflg = true;
                }
                if (getDate(-31).equals(data.get_id())) {
                    DataStatistics dataStatistics4 = new DataStatistics();
                    Source get_source = data.get_source();
                    dataStatistics4.setCount(dataStatistics.getCount() - get_source.getCount());
                    dataStatistics4.setSize(getDouble(dataStatistics.getSize() - get_source.getSize()));
                    dataStatistics4.setDatasets(dataStatistics.getDatasets() - get_source.getDatasets().size());
                    map.put("monthly", dataStatistics4);
                    monthflg = true;
                }
            }
            if (!yesflg) {
                DataStatistics dataStatistics_yes = new DataStatistics();
                dataStatistics_yes.setCount(dataStatistics.getCount());
                dataStatistics_yes.setSize(dataStatistics.getSize());
                dataStatistics_yes.setDatasets(dataStatistics.getDatasets());
                map.put("daily", dataStatistics_yes);
            }
            if (!weekflg) {
                long lastTime = format.parse(getDate(-1)).getTime();
                Source lastSource = new Source();
                String lastDate = getDate(-1);
                for (Hits data : jsonArray) {
                    String get_id = data.get_id();
                    long time = format.parse(get_id).getTime();
                    if (time < lastTime && time > format.parse(getDate(-8)).getTime()) {
                        lastTime = time;
                        lastDate = get_id;
                        lastSource = data.get_source();
                    }
                }
                DataStatistics dataStatistics5 = new DataStatistics();
                if (!getDate(-1).equals(lastDate)) {
                    dataStatistics5.setCount(dataStatistics.getCount() - lastSource.getCount());
                    dataStatistics5.setSize(getDouble(dataStatistics.getSize() - lastSource.getSize()));
                    dataStatistics5.setDatasets(dataStatistics.getDatasets() - lastSource.getDatasets().size());
                } else {
                    dataStatistics5.setCount(dataStatistics.getCount());
                    dataStatistics5.setSize(dataStatistics.getSize());
                    dataStatistics5.setDatasets(dataStatistics.getDatasets());
                }
                map.put("weekly", dataStatistics5);
            }
            if (!monthflg) {
                long lastTime = format.parse(getDate(-1)).getTime();
                Source lastSource = new Source();
                String lastDate = getDate(-1);
                for (Hits data : jsonArray) {
                    String get_id = data.get_id();
                    long time = format.parse(get_id).getTime();
                    if (time < lastTime && time > format.parse(getDate(-31)).getTime()) {
                        lastTime = time;
                        lastDate = get_id;
                        lastSource = data.get_source();
                    }
                }
                DataStatistics dataStatistics6 = new DataStatistics();
                if (!getDate(-1).equals(lastDate)) {
                    dataStatistics6.setCount(dataStatistics.getCount() - lastSource.getCount());
                    dataStatistics6.setSize(getDouble(dataStatistics.getSize() - lastSource.getSize()));
                    dataStatistics6.setDatasets(dataStatistics.getDatasets() - lastSource.getDatasets().size());
                } else {
                    dataStatistics6.setCount(dataStatistics.getCount());
                    dataStatistics6.setSize(dataStatistics.getSize());
                    dataStatistics6.setDatasets(dataStatistics.getDatasets());
                }
                map.put("monthly", dataStatistics6);
            }
        }

        return map;
    }
}