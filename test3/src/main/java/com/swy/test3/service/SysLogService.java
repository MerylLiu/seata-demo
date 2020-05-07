package com.swy.test3.service;

import com.jds.core.common.KendoResult;
import com.jds.core.service.BaseService;

import java.util.Map;

/**
 * Created by 宸宇 on 2019/12/16.
 */
public interface SysLogService extends BaseService {
    KendoResult getSysLogList(Map map);

    Map getSysLog(Map map);

    void delete();
}
