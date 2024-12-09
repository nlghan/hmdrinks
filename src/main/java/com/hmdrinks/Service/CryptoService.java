package com.hmdrinks.Service;

import com.hmdrinks.Enum.ResponseCode;
import com.hmdrinks.Exception.BusinessException;
import com.hmdrinks.Request.InitPaymentRequest;
import com.hmdrinks.Response.InitPaymentResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;

@Service
public class CryptoService {
    public static  class DefaultValue {

        public static final String HMAC_SHA512 = "HmacSHA512";
    }
        private final Mac mac = Mac.getInstance(DefaultValue.HMAC_SHA512);

        @Value("${payment.vnpay.secret-key}")
        private String secretKey;

        public CryptoService() throws NoSuchAlgorithmException {
        }

        @PostConstruct
        void init() throws InvalidKeyException {
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), DefaultValue.HMAC_SHA512);
            mac.init(secretKeySpec);
        }

        public String sign(String data) {
            try {
                return EncodingUtil.toHexString(mac.doFinal(data.getBytes()));
            }
            catch (Exception e) {
                throw new BusinessException(ResponseCode.VNPAY_SIGNING_FAILED);
            }
        }

    public static class EncodingUtil {

        public static String toHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

}