package com.hmdrinks.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hmdrinks.Response.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProvinceService {
    private static final String API_URL = "https://provinces.open-api.vn/api/p/";

    public ResponseEntity<?> fetchProvinces() {
        List<ProvinceResponse> provinceList = new ArrayList<>();

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();
            JsonArray jsonResponse = JsonParser.parseString(response.toString()).getAsJsonArray();
            for (JsonElement element : jsonResponse) {
                JsonObject province = element.getAsJsonObject();
                int provinceCode = province.get("code").getAsInt();
                String provinceName = province.get("name").getAsString();

                provinceList.add(new ProvinceResponse(provinceCode, provinceName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllProvinceResponse(provinceList));
    }



    public ResponseEntity<?> fetchDistricts(String provinceCode) {
        String API_URL_DISTRICT_BASE = "https://provinces.open-api.vn/api/d/search/?q=";
        List<DistrictResponse> districtList = new ArrayList<>();
        String[] queryTypes = {"Quận", "Huyện", "Thành phố", "Thị xã"};
        try {
            for (String query : queryTypes) {
                URL url = new URL(API_URL_DISTRICT_BASE + URLEncoder.encode(query, "UTF-8") + "&p=" + provinceCode);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("Lỗi HTTP: " + responseCode + " khi gọi URL: " + url);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi gọi API: " + responseCode);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        response.append(output);
                    }

                    JsonArray jsonResponse = JsonParser.parseString(response.toString()).getAsJsonArray();
                    for (JsonElement element : jsonResponse) {
                        JsonObject district = element.getAsJsonObject();
                        int districtId = district.get("code").getAsInt();
                        String districtName = district.get("name").getAsString();

                        districtList.add(new DistrictResponse(districtId, districtName));
                    }
                }
                conn.disconnect();
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi kết nối hoặc đọc dữ liệu từ API.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách quận/huyện.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllDistrictResponse(districtList));
    }



    public ResponseEntity<?> fetchWard(String districtId) {
        String API_URL_WARD_BASE = "https://provinces.open-api.vn/api/w/search/?q=";
        List<WardResponse> wardList = new ArrayList<>();
        String[] queryTypes = {"Phường", "Xã", "Thị trấn"};
        try {
            for (String query : queryTypes) {
                // Tạo URL đầy đủ cho API với từng loại tìm kiếm
                URL url = new URL(API_URL_WARD_BASE + URLEncoder.encode(query, "UTF-8") + "&d=" + districtId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.err.println("Lỗi HTTP: " + responseCode + " khi gọi URL: " + url);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi gọi API: " + responseCode);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        response.append(output);
                    }

                    JsonArray jsonResponse = JsonParser.parseString(response.toString()).getAsJsonArray();
                    for (JsonElement element : jsonResponse) {
                        JsonObject ward = element.getAsJsonObject();
                        int wardId = ward.get("code").getAsInt();
                        String wardName = ward.get("name").getAsString();

                        wardList.add(new WardResponse(wardId, wardName));
                    }
                }
                conn.disconnect();
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi kết nối hoặc đọc dữ liệu từ API.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách phường/xã/thị trấn.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ListAllWardResponse(wardList));
    }

}
