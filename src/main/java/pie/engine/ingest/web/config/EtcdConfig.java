package pie.engine.ingest.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pie.engine.ingest.web.utils.IngestEtcdTool;

@Configuration
public class EtcdConfig {
    @Value("${etcd.ip}")
    private String etcdIp;

    @Value("${etcd.port}")
    private Integer etcdPort;

    @Bean
    public IngestEtcdTool etcdTool() {
        IngestEtcdTool ingestEtcdTool = new IngestEtcdTool();
        ingestEtcdTool.connectToEtcd(etcdIp, etcdPort);
        
        return ingestEtcdTool;
    }

    public void setEtcdIp(String etcdIp) {
        this.etcdIp = etcdIp;
    }

    public String getEtcdIp() {
        return etcdIp;
    }

    public void setEtcdPort(Integer etcdPort) {
        this.etcdPort = etcdPort;
    }

    public Integer getEtcdPort() {
        return etcdPort;
    }
}