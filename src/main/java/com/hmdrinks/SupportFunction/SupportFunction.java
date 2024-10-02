package com.hmdrinks.SupportFunction;

public class SupportFunction {
    public static boolean checkRole(String role) {
        boolean checkResult = true;
            if (!role.equals("ADMIN") && !role.equals("CUSTOMER") && !role.equals("SHIPPER") ) {
                checkResult = false;

        }
        return checkResult;
    }
}
