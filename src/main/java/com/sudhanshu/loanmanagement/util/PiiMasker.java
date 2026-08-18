package com.sudhanshu.loanmanagement.util;

/**
 * Masks sensitive PII before logging.
 */
public final class PiiMasker {

    private PiiMasker() {}

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return "**" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    public static String maskPan(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        return "XXXXXX" + pan.substring(pan.length() - 4);
    }

    public static String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return "****";
        }
        return "XXXXXXXX" + aadhaar.substring(aadhaar.length() - 4);
    }

    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) {
            return "****";
        }
        return "******" + mobile.substring(mobile.length() - 4);
    }
}
