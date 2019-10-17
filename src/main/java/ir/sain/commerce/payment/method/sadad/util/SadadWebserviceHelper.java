package ir.sain.commerce.payment.method.sadad.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONObject;
import jdk.nashorn.internal.parser.JSONParser;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SadadWebserviceHelper {

    public String getMelliTokenURL()
            throws SystemException, PortalException {
//        return PortletPropsValues.MELLI_EPAYMENT_WEB_SERVICE_TOKEN_URL;
        return "https://sadad.shaparak.ir/vpg/api/v0/Request/PaymentRequest";
    }
    public String getMelliGetURL()
            throws SystemException, PortalException {
//        return PortletPropsValues.MELLI_EPAYMENT_GET_URL;
        return PortletPropsValues.MELLI_EPAYMENT_GET_URL;
    }

    public String[] getMelliToken(long bankProfileId,String invoiceNumber,String revertURL,long amount)
            throws Exception {
        BankProfile bankProfile = getBankProfile(bankProfileId);
        String[] results = new String[2];
        try {
            String signedData = tripleDesEncrypt(String.format("%s;%s;%s", bankProfile.getAccountNum(), invoiceNumber, amount), bankProfile.getPrivateKey());
            JSONObject data = new JSONObject();
            data.put("TerminalId", bankProfile.getAccountNum());
            data.put("MerchantId", bankProfile.getMerchantId());
            data.put("Amount", amount);
            data.put("SignData", signedData);
            data.put("ReturnUrl", revertURL);
            data.put("OrderId", invoiceNumber);
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ").format(new Date());
            String tail = date.substring(date.indexOf("+") + 1);
            tail = tail.substring(0, 2) + ":" + tail.substring(2);
            date = date.substring(0, date.indexOf("+") + 1) + tail;
            data.put("LocalDateTime", date);

            data = callRestApi(PortletPropsValues.MELLI_EPAYMENT_WEB_SERVICE_TOKEN_URL, data);
            results[0]=(String)data.get("ResCode");
            results[1]=(String)data.get("Token");
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return results;
    }

    public String[] melliVerify(long bankProfileId, String token)
            throws Exception {
        BankProfile bankProfile = getBankProfile(bankProfileId);
        String[] responseVerifyArray = new String[5];
        String signedData = tripleDesEncrypt(token, bankProfile.getPrivateKey());
        JSONObject data = new JSONObject();
        data.put("token", token);
        data.put("SignData", signedData);

        data = callRestApi(PortletPropsValues.MELLI_EPAYMENT_WEB_SERVICE_VERIFY_URL, data);
        if (data != null) {
            responseVerifyArray[0] = (String) data.get("ResCode");
            responseVerifyArray[1] = (String) data.get("OrderId");
            responseVerifyArray[2] = (String) data.get("Amount");
            responseVerifyArray[3] = (String) data.get("RetrivalRefNo");
            responseVerifyArray[4] = (String) data.get("SystemTraceNo");
        }
        return responseVerifyArray;
    }

    private String tripleDesEncrypt(String message, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(org.bouncycastle.util.encoders.Base64.decode(key.getBytes()), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS7Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] buf = cipher.doFinal(message.getBytes("UTF-8"));
        return  org.bouncycastle.util.encoders.Base64.toBase64String(buf);
    }

    private JSONObject callRestApi(String urlString, JSONObject data) {
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
                Reader reader = new InputStreamReader(connection.getInputStream(),"UTF-8");
                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(reader);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
