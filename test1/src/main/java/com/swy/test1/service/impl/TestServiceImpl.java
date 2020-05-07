package com.swy.test1.service.impl;

import com.jds.core.service.impl.BaseServiceImpl;
import com.jds.core.utils.DateUtil;
import com.swy.test1.service.TestService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TestServiceImpl extends BaseServiceImpl implements TestService {
    @Autowired
    RestTemplate restTemplate;

    @Override
//    @GlobalTransactional
//    @Transactional
    public void txTest() {
        String xid = RootContext.getXID();
        System.out.println("===============XID:" + xid + "==================");

        Map map = new HashMap<>();
        map.put("name", "meryl");
        map.put("content", "hello word");
        map.put("time", DateUtil.getNow());
        db("posts").insert(map);

        String url = "http://test2/save";
        Map param = new HashMap();
        String res = restTemplate.postForObject(url, param, String.class);

        Map map2 = new HashMap<>();
        map2.put("name", "meryl");
        map2.put("password", "hello word");
        map2.put("status", "up");
        map2.put("imagePath", DateUtil.getNow());
        db("user").insert(map2);
    }

    @Override
    @GlobalTransactional
    public void test() {
        String xid = RootContext.getXID();
        System.out.println("===============XID:" + xid + "==================");

        Map map = new HashMap<>();
        map.put("name", "meryl");
        map.put("content", "hello word");
        map.put("time", DateUtil.getNow());
        db("posts").insert(map);

        String url = "http://test2/test";
        Map param = new HashMap();
        String res = restTemplate.postForObject(url, param, String.class);

        Map map2 = new HashMap<>();
        map2.put("name", "meryl");
        map2.put("password", "hello word");
        map2.put("status", "up");
        map2.put("imagePath", DateUtil.getNow());
        db("user").insert(map2);
    }
}
