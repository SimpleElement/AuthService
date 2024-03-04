package org.example.authservice.common.util;

public class NumberUtil {

    public static int[] getArrRandomInt(int sizeArr) {
        int[] res = new int[sizeArr];

        for (int i = 0; i < res.length; i++) {
            res[i] = (int) (Math.random() * 10);
        }

        return res;
    }
}
