package io.notfound.counsel_back.payment.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class PortoneService {

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private static final String API_KEY = "1105723582517465"; // REST API Key
    private static final String API_SECRET = "gnwDsK0xpbbMq3ag3Oj2xJPNL1JtQuOkCVShpz5m0cqNhNgoLgP6TmnCAy9I2vrx30udpXUtkSyau3L3"; // REST API Secret

    public String getAccessToken() throws IOException {
        String url = "https://api.iamport.kr/users/getToken";

        String json = "{\"imp_key\":\"" + API_KEY + "\",\"imp_secret\":\"" + API_SECRET + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        // 실제 코드에서는 JSON 파싱 로직을 추가하여 토큰을 반환해야 합니다.
        return gson.fromJson(response.body().string(), JsonObject.class)
                .get("response").getAsJsonObject().get("access_token").getAsString();
    }

    public boolean verifyPayment(String impUid, String accessToken) throws IOException {
        String url = "https://api.iamport.kr/payments/" + impUid;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", accessToken)
                .build();

        Response response = client.newCall(request).execute();
        String bodyStr = response.body().string();
        System.out.println("Portone verifyPayment response: " + bodyStr); // ✅ 로그 추가

        JsonObject json = gson.fromJson(bodyStr, JsonObject.class);

        if (!json.has("response") || json.get("response").isJsonNull()) {
            throw new IllegalStateException("Not a JSON Object: " + bodyStr);
        }

        return json.getAsJsonObject("response").get("status").getAsString().equals("paid");
    }

}