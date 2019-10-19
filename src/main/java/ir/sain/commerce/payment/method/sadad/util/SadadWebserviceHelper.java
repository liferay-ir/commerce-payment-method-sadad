package ir.sain.commerce.payment.method.sadad.util;

import com.fasterxml.jackson.core.JsonToken;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.text.StrBuilder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SadadWebserviceHelper {

    public static String getMelliTokenURL()
            throws SystemException, PortalException {
//        return PortletPropsValues.MELLI_EPAYMENT_WEB_SERVICE_TOKEN_URL;
        return "https://sadad.shaparak.ir/vpg/api/v0/Request/PaymentRequest";
    }

    public static String getMelliGetURL()
            throws SystemException, PortalException {
//        return PortletPropsValues.MELLI_EPAYMENT_GET_URL;
        return "https://sadad.shaparak.ir/VPG/Purchase";
    }

    public static String getMelliVerifyURL()
            throws SystemException, PortalException {
//        return PortletPropsValues.MELLI_EPAYMENT_GET_URL;
        return "https://sadad.shaparak.ir/vpg/api/v0/Advice/Verify";
    }

    public static String[] getMelliToken(String invoiceNumber, String revertURL, String terminalId, String merchantId,String merchantKey, long amount)
            throws Exception {

        String[] results = new String[2];
        try {
            String signedData = tripleDesEncrypt(String.format("%s;%s;%s", terminalId, invoiceNumber, amount), merchantKey);
            JSONObject data = JSONFactoryUtil.createJSONObject();
            data.put("TerminalId", terminalId);
            data.put("MerchantId", merchantId);
            data.put("Amount", amount);
            data.put("SignData", signedData);
            data.put("ReturnUrl", revertURL);
            data.put("OrderId", invoiceNumber);
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ").format(new Date());
            String tail = date.substring(date.indexOf("+") + 1);
            tail = tail.substring(0, 2) + ":" + tail.substring(2);
            date = date.substring(0, date.indexOf("+") + 1) + tail;
            data.put("LocalDateTime", date);

            data = callRestApi(getMelliTokenURL(), data);
            results[0] = (String) data.get("ResCode");
            results[1] = (String) data.get("Token");
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return results;
    }

    public static String[] melliVerify(String token, String merchantKey)
            throws Exception {
        String[] responseVerifyArray = new String[5];
        String signedData = tripleDesEncrypt(token, merchantKey);
        JSONObject data = JSONFactoryUtil.createJSONObject();
        data.put("token", token);
        data.put("SignData", signedData);

        data = callRestApi(getMelliVerifyURL(), data);
        if (data != null) {
            responseVerifyArray[0] = (String) data.get("ResCode");
            responseVerifyArray[1] = (String) data.get("OrderId");
            responseVerifyArray[2] = (String) data.get("Amount");
            responseVerifyArray[3] = (String) data.get("RetrivalRefNo");
            responseVerifyArray[4] = (String) data.get("SystemTraceNo");
        }
        return responseVerifyArray;
    }

    private static String tripleDesEncrypt(String message, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(org.bouncycastle.util.encoders.Base64.decode(key.getBytes()), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS7Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] buf = cipher.doFinal(message.getBytes("UTF-8"));
        return  org.bouncycastle.util.encoders.Base64.toBase64String(buf);
    }

    private static JSONObject callRestApi(String urlString, JSONObject data) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(data.toString().replace("\\/", "/").getBytes());
            os.flush();

            if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
                System.out.println("connection *************** = " + connection.getResponseCode());
                Reader reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
                StringBuffer sb = new StringBuffer();
                BufferedReader input = new BufferedReader(reader);
//                TODO must replace with json parser
                String line = "";
                while ((line = input.readLine()) != null) {
                    System.out.println("line = " + line);
                    sb.append(line);
                }

                return JSONFactoryUtil.createJSONObject(sb.toString());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
