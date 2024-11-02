package com.hmdrinks.SupportFunction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Exception.ConflictException;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;


@Component
public class SupportFunction {
    private final JwtService jwtService;
    private final String apiKey = "VudYm4ZnWzUU2Rv5HmxxV2IwrK834KcKmuUQMkGG";
    @Autowired
    public SupportFunction(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public boolean checkRole(String role) {
        return role.equals("ADMIN") || role.equals("CUSTOMER") || role.equals("SHIPPER");
    }

    public ResponseEntity<?> checkUserAuthorization(HttpServletRequest httpRequest, int userIdFromRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid");
        }
        String jwt = authHeader.substring(7);
        String userIdFromTokenStr = jwtService.extractUserId(jwt);
        int userIdFromToken;
        try {
            userIdFromToken = Integer.parseInt(userIdFromTokenStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID format in token");
        }

        if (userIdFromRequest != userIdFromToken) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to perform this action");
        }
        else
        {
            return ResponseEntity.status(HttpStatus.OK).body("You have successfully logged in");
        }
    }

    public ResponseEntity<?> checkPhoneNumber(String phoneNumber, Integer userId, UserRepository userRepository) {
        if (phoneNumber == null || phoneNumber.length() != 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số điện thoại không hợp lệ. Phải chứa 10 chữ số.");
        }
        Optional<User> existingUserOptional = userRepository.findByPhoneNumberAndIsDeletedFalse(phoneNumber);
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            if (!(existingUser.getUserId() ==userId)) {
                throw new ConflictException("Số điện thoại đã tồn tại.");
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public String getLocation(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");

            String location = "21.013715429594125,105.79829597455202";
            String urlString = "https://rsapi.goong.io/Place/AutoComplete?api_key=" + apiKey
                    + "&location=" + location + "&input=" + encodedAddress;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray predictions = jsonResponse.getAsJsonArray("predictions");
            if (predictions != null && predictions.size() > 0) {
                for (int i = 0; i < predictions.size(); i++) {
                    JsonObject prediction = predictions.get(i).getAsJsonObject();
                    if (prediction.has("place_id")) {
                        String placeId = prediction.get("place_id").getAsString();
                        return placeId;
                    }
                }
            } else {
                System.out.println("Không tìm thấy trường 'predictions' hoặc không có dữ liệu.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double[] getCoordinates(String placeId) {
        try {
            String apiKey = "VudYm4ZnWzUU2Rv5HmxxV2IwrK834KcKmuUQMkGG";
            String urlString = "https://rsapi.goong.io/geocode?place_id=" + placeId + "&api_key=" + apiKey;

            URL url = new URL(urlString);
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
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            if (jsonResponse.has("results") && jsonResponse.getAsJsonArray("results").size() > 0) {
                JsonObject location = jsonResponse.getAsJsonArray("results")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("geometry")
                        .getAsJsonObject("location");

                double lat = location.get("lat").getAsDouble();
                double lng = location.get("lng").getAsDouble();

                return new double[]{lat, lng};
            } else {
                System.out.println("Không có kết quả nào được trả về.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double getShortestDistance(double[] origins, double[] destinations) {
        double shortestDistanceValue = Double.MAX_VALUE;
        try {
            String apiKey = "VudYm4ZnWzUU2Rv5HmxxV2IwrK834KcKmuUQMkGG";
            String originsParam = origins[0] + "," + origins[1];
            String destinationsParam = destinations[0] + "," + destinations[1];

            String urlString = "https://rsapi.goong.io/DistanceMatrix?origins=" + originsParam +
                    "&destinations=" + destinationsParam +
                    "&vehicle=car&api_key=" + apiKey;
            URL url = new URL(urlString);
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

            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray rows = jsonResponse.getAsJsonArray("rows");

            if (rows.size() > 0) {
                JsonArray elements = rows.get(0).getAsJsonObject().getAsJsonArray("elements");

                for (JsonElement element : elements) {
                    JsonObject elementObj = element.getAsJsonObject();
                    if (elementObj.get("status").getAsString().equals("OK")) {
                        String distanceText = elementObj.getAsJsonObject("distance").get("text").getAsString();
                        double distanceValue;

                        if (distanceText.contains("km")) {
                            distanceValue = Double.parseDouble(distanceText.replace(" km", "").trim());
                        } else if (distanceText.contains("m")) {
                            distanceValue = Double.parseDouble(distanceText.replace(" m", "").trim()) / 1000; // Chuyển đổi từ m sang km
                        } else {
                            continue;
                        }
                        if (distanceValue < shortestDistanceValue) {
                            shortestDistanceValue = distanceValue;
                        }
                    }
                }
            } else {
                System.out.println("Không có kết quả nào được trả về.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return shortestDistanceValue != Double.MAX_VALUE ? shortestDistanceValue : -1;
    }
}