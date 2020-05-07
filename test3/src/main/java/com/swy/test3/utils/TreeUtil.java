package com.swy.test3.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author : Meryl
 * @Description:
 * @Date: Created in 2019/12/12
 * @Modify by :
 */
public class TreeUtil {

    public static <T extends Map> List<T> formatTree(List<T> list, boolean expand) {
        List<T> nodeList = new ArrayList<T>();
        for (T node1 : list) {
            boolean mark = false;
            for (T node2 : list) {
                if (node1.get("parent_id") != null && node1.get("parent_id").equals(node2.get("id"))) {
                    node2.put("is_leaf", 0);
                    mark = true;

                    if (node2.get("children") == null) {
                        node2.put("children", new ArrayList<T>());
                    }
                    ArrayList children = (ArrayList) node2.get("children");
                    children.add(node1);
                    node2.put("children", children);

                    if (expand) {
                        node2.put("open", true);
                    } else {
                        node2.put("open", false);
                    }
                    break;
                }
            }

            if (!mark) {
                nodeList.add(node1);
                if (expand) {
                    //all expand
                }
            } else {
                node1.put("open", false);
            }
        }
        return nodeList;
    }
}
