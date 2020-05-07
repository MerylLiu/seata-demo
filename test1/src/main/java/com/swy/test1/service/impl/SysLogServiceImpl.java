package com.swy.test1.service.impl;

import com.jds.core.common.KendoResult;
import com.jds.core.service.impl.BaseServiceImpl;
import com.jds.core.utils.BaseUtil;
import com.jds.core.utils.QueryUtil;
import com.swy.test1.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by 宸宇 on 2019/12/16.
 */

@Service
public class SysLogServiceImpl extends BaseServiceImpl implements SysLogService {
    @Override
    public KendoResult getSysLogList(Map param) {
        Integer page = BaseUtil.isNullOrEmpty(param.get("page"), 1, Integer.class);
        Integer pageSize = BaseUtil.isNullOrEmpty(param.get("pageSize"), 20, Integer.class);

        KendoResult data = QueryUtil.getRecordsPaged("sysLog.getSysLogListPaged", param, page, pageSize);

        return data;
    }

    @Override
    public Map getSysLog(Map map) {
        Map data = db("sys_log").find("Id = #{id}", map.get("id"));
        return data;
    }

    @Override
    public void delete() {
        db().delete("sysLog.deleteAllSysLog");
    }
}
