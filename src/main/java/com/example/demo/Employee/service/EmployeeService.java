package com.example.demo.Employee.service;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.Employee.data.Attendance;
import com.example.demo.Employee.data.Employees;
import com.example.demo.Employee.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmployeeService {

	private final EmployeeRepository employeeRepository;

	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public List<String> getEmployees() throws IOException {
		return employeeRepository.getEmployees();
	}

	public Employees getEmployeeDetail(int id) throws IOException {
		Employees employee = employeeRepository.getEmployeeDetail(id);
		List<Attendance> attendances = employeeRepository.getEmployeeAttendance(id);
		if (employee != null) {
			Attendance latestAttendance = !attendances.isEmpty() ? attendances.get(attendances.size() - 1) : null;
			employee.setAttendance(latestAttendance);
		}

		return employee;
	}

	public boolean addAttendance(Attendance attendance) throws IOException {
		String url = "https://jsn9xu2vsk.execute-api.ap-northeast-1.amazonaws.com/sample/attendanceandabsence/clock";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String clockIn = attendance.getClockIn() != null ? attendance.getClockIn() : "";
		String breakStart = attendance.getBreakStart() != null ? attendance.getBreakStart() : "";
		String breakEnd = attendance.getBreakEnd() != null ? attendance.getBreakEnd() : "";
		String clockOut = attendance.getClockOut() != null ? attendance.getClockOut() : "";

		// Create a request object with the given attendance data
		String requestBody = "{\"body\":\"{\\\"employee_id\\\":\\\"" + attendance.getEmployeeId() +
				"\\\",\\\"clock_in\\\":\\\"" + clockIn +
				"\\\",\\\"break_start\\\":\\\"" + breakStart +
				"\\\",\\\"break_end\\\":\\\"" + breakEnd +
				"\\\",\\\"clock_out\\\":\\\"" + clockOut + "\\\"}\"}";
		System.out.println(requestBody);
		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
		String responseBody = responseEntity.getBody();

		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(responseBody);

		if (root.has("error")) {
			// API returned an error message
			System.out.println("API Error: " + root.get("error").asText());
			return false;
		} else {
			// API successfully added attendance
			System.out.println("Success");
			return true;
		}

	}

}
