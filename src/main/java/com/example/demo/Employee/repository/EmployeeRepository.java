package com.example.demo.Employee.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.example.demo.Employee.data.Attendance;
import com.example.demo.Employee.data.Employees;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class EmployeeRepository {

    public List<String> getSpecificPropertiesFromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        List<String> propertiesList = new ArrayList<>();
        for (JsonNode node : rootNode) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                if (!fieldName.equals("created_at") && !fieldName.equals("updated_at")) {
                    JsonNode propertyNode = node.get(fieldName);
                    System.out.println(propertyNode);
                    if (propertyNode != null && propertyNode.isValueNode()) {
                        String propertyValue;
                        if (fieldName.equals("id")) {
                            propertyValue = String.valueOf(propertyNode.asInt());
                        } else {
                            propertyValue = propertyNode.asText();
                        }
                        propertiesList.add(propertyValue);
                    }
                }
            }
        }

        return propertiesList;
    }

    public List<String> getEmployees() throws IOException {
        String url = "https://jsn9xu2vsk.execute-api.ap-northeast-1.amazonaws.com/sample/attendanceandabsence/employee";

        RestTemplate rest = new RestTemplate();
        ResponseEntity<String> response = rest.getForEntity(url, String.class);
        String json = response.getBody();
        return getSpecificPropertiesFromJson(json);
    }

    public Employees getEmployeeDetail(int id) throws IOException {
        String url = "https://jsn9xu2vsk.execute-api.ap-northeast-1.amazonaws.com/sample/attendanceandabsence/employee";

        RestTemplate rest = new RestTemplate();
        ResponseEntity<String> response = rest.getForEntity(url, String.class);
        String json = response.getBody();

        ObjectMapper mapper = new ObjectMapper();
        List<Employees> employees = mapper.readValue(json, new TypeReference<List<Employees>>() {});

        return employees.stream()
                .filter(employee -> employee.getId() == id)
                .findFirst()
                .orElse(null);
    }


    public List<Attendance> getEmployeeAttendance(int employeeId) throws IOException {
        String attendanceUrl = "https://jsn9xu2vsk.execute-api.ap-northeast-1.amazonaws.com/sample/attendanceandabsence/clock?employeeId=" + employeeId;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> attendanceResponse = restTemplate.getForEntity(attendanceUrl, String.class);
        String attendanceJson = attendanceResponse.getBody();
        ObjectMapper mapper = new ObjectMapper();
        List<Attendance> attendances = mapper.readValue(attendanceJson, new TypeReference<List<Attendance>>() {});
        Collections.reverse(attendances);
        return attendances;
    }
}