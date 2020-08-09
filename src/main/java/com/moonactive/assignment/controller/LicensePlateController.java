package com.moonactive.assignment.controller;

import com.moonactive.assignment.converter.mapper.LicensePlateRowMapper;
import com.moonactive.assignment.dao.LicensePlate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/plates")
public class LicensePlateController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private

    @Autowired
    JdbcTemplate jdbcTemplate;
    // Displays the list of all the vehicles who tried to enter
    @GetMapping("/list")
    public List<LicensePlate> getPlates() {
        List<LicensePlate> plates = jdbcTemplate.query("SELECT * from LICENSE_PLATE",new LicensePlateRowMapper());
        return plates;
    }

    // Checks whether the vehicle is allowed to enter
    @GetMapping("/check")
    public String checkPlate(@RequestParam String image_url) {
        String response = null;
        int count = 0;
        int maxTries = 3;
        while (true) {
            try {
                response = postRequest(image_url);
                JSONObject object = new JSONObject(response);
                if(object.has("ParsedResults") && object.getJSONArray("ParsedResults").length() > 0 ) {
                    response = ((JSONObject)((JSONArray) object.get("ParsedResults")).get(0)).getString("ParsedText");
                    break;
                }
                else if (object.has("IsErroredOnProcessing")
                        && object.getBoolean("IsErroredOnProcessing"))
                {
                    response = null;
                    throw new Exception();
                }
            }
            catch (Exception e) {
                response = null;
                if (++count == maxTries)
                    break;
            }
        }
        if(response == null || response.isEmpty()) {
            return "OCR API failed 3 times due to timeouts.";
        }

        String plateNumber = editPlateNumber(response);

        if(plateNumber.length() < 7 || plateNumber.equals("No digits")) {
            return "Invalid license plate - not a license plate or not an Israeli license plate. Please insert a valid Israeli license plate.";
        }
        String plateType = determineType(plateNumber);

        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        try {
            jdbcTemplate.update(
                    "INSERT INTO LICENSE_PLATE (PLATE_ID, LICENSE_TYPE, TS) VALUES (?, ?, ?)",
                    plateNumber, plateType, currentTimestamp
            );
        } catch (Exception e)
        {
            log.error("Spring Security Filter Chain Exception:", e);
        }

        if(!plateType.equals("Allowed"))
            return plateType + " vehicle is NOT allowed to enter the parking lot.";

        return "Vehicle number "+plateNumber+" is allowed to enter the parking lot.";
    }

    // Edits the string in order to retrieve the desired plate number
    public String editPlateNumber(String response){
        String[] strArrayForNewLine = response.split("\n", 2);
        response = strArrayForNewLine[0];
        String[] strArrayForTabs = response.split("\t", 2);
        response = strArrayForTabs[0];
        String plateNumber = response.replaceAll("\\s+","");
        plateNumber = plateNumber.replaceAll(":", "");
        plateNumber = plateNumber.replaceAll("-", "");
        plateNumber = plateNumber.replaceAll("[^\\dA-Za-z ]", "");
        if(!containsDigits(response)) {
            return "No digits";
        }
        if(response.startsWith("IL")) {
            plateNumber = plateNumber.substring(2);
        }
        if (plateNumber.length() > 8) {
            plateNumber = plateNumber.substring(0, 8);
        }

        return plateNumber;
    }

    // Checks whether the license plate number contains digits
    public boolean containsDigits(String response){
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(response);
        return m.find();
    }

    // Determines the vehicle's type
    public String determineType(String toDetermine){
        // Military case
        Pattern p = Pattern.compile("[a-zA-Z]+");
        Matcher m = p.matcher(toDetermine);
        if (m.find()) {
            return "Military";
        }
        String lastTwoDigits = toDetermine.substring(toDetermine.length() - 2);
        // Public Transportation case
        if (lastTwoDigits.equals("25") || lastTwoDigits.equals("26")){
            return "PublicTransportation";
        }
        // Prohibited Digits case
        if (containsProhibitedDigits(lastTwoDigits))
            return "ProhibitedDigits";
        // Gas case
        if (dividedBySeven(toDetermine))
            return "Gas";

        // Allowed case
        return "Allowed";
    }

    // Checks last two digits
    public boolean containsProhibitedDigits(String lastTwoDigits){
        return (lastTwoDigits.equals("85") || lastTwoDigits.equals("86") || lastTwoDigits.equals("87") || lastTwoDigits.equals("88") || lastTwoDigits.equals("89") || lastTwoDigits.equals("00"));
    }

    // Checks whether sum divided by seven
    public boolean dividedBySeven(String toDetermine){
        int sum = 0;

        for(int i = 0; i < toDetermine.length(); i++) {
            char ch = toDetermine.charAt(i);
            sum += Character.getNumericValue(ch);
        }
        return (sum % 7) == 0;
    }


    // Connects to OCR
    public static String postRequest(String image_url) throws Exception{
        URL url = new URL("https://api.ocr.space/parse/image");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setRequestProperty("User-Agent", "Mozilla/5.0");
        http.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        JSONObject postDataParams = new JSONObject();
        postDataParams.put("apikey", "0fd74c6e9e88957");
        postDataParams.put("isOverlayRequired", true);
        postDataParams.put("scale", true);
        postDataParams.put("url", image_url);
        postDataParams.put("language", "eng");
        postDataParams.put("isTable", true);
        postDataParams.put("OCREngine", 2);
        http.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(getPostDataString(postDataParams));
        wr.flush();
        wr.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return String.valueOf(response);
    }

    public static String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}