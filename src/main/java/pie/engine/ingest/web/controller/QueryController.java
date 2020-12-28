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
@Api(tags="ingest信息查询")
public class QueryController {
    @Autowired
    private IBatchService batchService;

    @GetMapping("/batch")
    @ApiOperation("Batch批次任务信息查询")
    public AjaxResult listBatch(
        @RequestParam(value = "type", required = false)String type,
        @RequestParam(value = "status", required = false)String status)
    {
        if (type == null || type.length() == 0) {
            return AjaxResult.success(batchService.getDataTypeList());
        }

        return AjaxResult.success(batchService.getBatchList(type, status));
    }
}