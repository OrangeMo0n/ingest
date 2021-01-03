package pie.engine.ingest.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import pie.engine.ingest.framework.core.domain.AjaxResult;
import pie.engine.ingest.web.service.IBatchService;

@RestController
@RequestMapping("query")
@Api(tags = "ingest信息查询")
public class QueryController {
    @Autowired
    private IBatchService batchService;

    @GetMapping("/batch/list")
    @ApiOperation("Batch批次任务信息查询")
    public AjaxResult listBatch(@RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status) {
        if (type == null || type.length() == 0) {
            return AjaxResult.success(batchService.getDataTypeList());
        }

        return AjaxResult.success(batchService.getBatchList(type, status));
    }

    @GetMapping("/batch")
    @ApiOperation("根据数据来源typeId和batchId获取batch信息")
    public AjaxResult getBatch(@RequestParam(value = "type", required = true) String type,
            @RequestParam(value = "id", required = true) String id) {
        return AjaxResult.success(batchService.getBatch(type, id));
    }

    @GetMapping("/task/list")
    @ApiOperation("根据数据来源typeId和batchId获取所有的task信息列表")
    public AjaxResult listTask(@RequestParam(value = "type", required = true) String type,
            @RequestParam(value = "id", required = true) String id) {
        return AjaxResult.success(batchService.getTaskList(type, id));
    }
}