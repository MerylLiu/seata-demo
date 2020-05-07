package com.swy.test1.utils;

import com.jds.core.utils.ConvertUtil;

/**
 * @Author : Meryl
 * @Description:
 * @Date: Created in 2019/12/17
 * @Modify by :
 */
public class CodeUtil {

    public static String getCode(String regular, String parentCode, Integer num) {
        String[] regulars = regular.split("-");
        String tempCode = parentCode;

        for (int i = 0; i < regulars.length; i++) {
            //pLength += ConvertUtil.parseInt(regulars[i]);
            int currLength = ConvertUtil.parseInt(regulars[i]);
            if (tempCode.length() >= currLength) {
                tempCode = tempCode.substring(currLength-1);
            } else {
                String str = String.format("%0" + currLength + "d", num);
                return parentCode + str;
            }
        }

        String str = String.format("%03d", num);
        return parentCode + str;
    }
}
