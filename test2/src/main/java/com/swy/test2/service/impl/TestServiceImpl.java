package com.swy.test2.service.impl;

import com.jds.core.common.BizException;
import com.jds.core.service.impl.BaseServiceImpl;
import com.swy.test2.service.TestService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TestServiceImpl extends BaseServiceImpl implements TestService {
    @Autowired
    private RestTemplate restTemplate;


    @Override
//    @GlobalTransactional
//    @Transactional
    public void txTest2() {
        Map map = new HashMap<>();
        map.put("name", "meryl");
        map.put("type", "hello");
        db("temp").insert(map);

        String xid = RootContext.getXID();
        System.out.println("===============XID:" + xid + "==================");


        String url = "http://test3/save";
        Map param = new HashMap();
        String res = restTemplate.postForObject(url, param, String.class);

        if (1 == 1) throw new BizException("errdddd test2");
    }

    @Override
    public void test2() {
        Map map = new HashMap<>();
        map.put("name", "meryl");
        map.put("type", "hello");
        db("temp").insert(map);

        String xid = RootContext.getXID();
        System.out.println("===============XID:" + xid + "==================");

        if (1 == 1) throw new BizException("测试错误异常回滚");
    }
}
