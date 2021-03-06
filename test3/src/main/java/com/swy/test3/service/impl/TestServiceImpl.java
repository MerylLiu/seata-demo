package com.swy.test3.service.impl;

import com.jds.core.common.BizException;
import com.jds.core.service.impl.BaseServiceImpl;
import com.swy.test3.service.TestService;
import io.seata.core.context.RootContext;
import org.apache.poi.sl.draw.geom.Guide;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TestServiceImpl extends BaseServiceImpl implements TestService {
    @Override
//    @GlobalTransactional
//    @Transactional
    public void txTest3() {
        Map map = new HashMap<>();
//        map.put("id", UUID.randomUUID().toString());
        map.put("gid", UUID.randomUUID().toString());
        map.put("name", "meryl");
        map.put("type", "hello");
        db("temp").insert(map);

        String xid = RootContext.getXID();
        System.out.println("===============XID:" + xid + "==================");

        Map test= new HashMap();
        test.put("cont","ddd");
        db("test").insert(test);


//        if (1 == 1) throw new BizException("errdddd test3");
    }

    @Override
    public void test3() {
        Map map = new HashMap<>();
        map.put("name", "meryl");
        map.put("type", "hello");
        db("temp").insert(map);

        String xid = RootContext.getXID();
        System.out.println("===============XID:" + xid + "==================");

        if (1 == 1) throw new BizException("测试错误异常回滚");
    }
}
