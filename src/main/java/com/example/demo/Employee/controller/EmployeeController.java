package com.example.demo.Employee.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Employee.data.Attendance;
import com.example.demo.Employee.data.Employees;
import com.example.demo.Employee.service.EmployeeService;

@Controller
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping("/employeeList")
	public String getEmployees(Model model) throws IOException {
		List<String> employeeList = employeeService.getEmployees();
		model.addAttribute("employeeList", employeeList);
		return "employeeList";
	}

	@GetMapping("/employeeDetail/{id}")
	public String getEmployeeDetail(@PathVariable("id") int id, Model model) throws IOException {
		Employees employeeDetail = employeeService.getEmployeeDetail(id);
		//		Attandance attandanceList = employeeDetail.getAttendance();
		model.addAttribute("employeeDetail", employeeDetail);
		//		model.addAllAttributes("attendance", attendance);
		return "employeeDetail";
	}

	@GetMapping("/employeeRegister")
	public String employeeRegisterPage() {
		return "employeeRegister";
	}

	@PostMapping("/employeeRegister")
	public String registerEmployee(@RequestParam("name") String name,
			@RequestParam("hometown") String hometown,
			@RequestParam("joining_month") String joiningMonth) throws IOException {
		// 入力データをAPIの要件に合わせて整形する
		String requestBody = "{\"body\": \"{\\\"name\\\":\\\"" + name + "\\\",\\\"hometown\\\":\\\"" + hometown
				+ "\\\",\\\"joining_month\\\":\\\"" + joiningMonth + "\\\"}\"}";

		// APIにデータを送信
		String apiUrl = "https://jsn9xu2vsk.execute-api.ap-northeast-1.amazonaws.com/sample/attendanceandabsence/employee";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
		String responseBody = responseEntity.getBody();

		// APIからのレスポンスを処理
		// ここで必要な処理を行う（成功か失敗かを判定するなど）

		// 新規登録後に社員一覧ページにリダイレクト
		return "redirect:/employeeList";
	}

	@PostMapping("/addAttendance")
	public String addAttendance(@RequestParam("id") int employeeId, @RequestParam("submit") String submit,
			RedirectAttributes redirectAttributes)
			throws IOException {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String attendanceTime = now.format(formatter);

		// データベースから社員の最新の出退勤情報を取得
		Employees employeeDetail = employeeService.getEmployeeDetail(employeeId);
		System.out.println(employeeDetail);
		Attendance latestAttendance = employeeDetail.getAttendance();
		System.out.println(latestAttendance);

		Attendance newAttendance = new Attendance();
		newAttendance.setEmployeeId(employeeId);

		if ("in".equals(submit)) {
			newAttendance.setClockIn(attendanceTime);
			System.out.println(newAttendance);
		}

		if ("break_start".equals(submit)) {
			newAttendance.setBreakStart(attendanceTime);
		}

		if ("break_end".equals(submit)) {
			newAttendance.setBreakEnd(attendanceTime);
		}

		if ("out".equals(submit)) {
			newAttendance.setClockOut(attendanceTime);
		}

		boolean success = employeeService.addAttendance(newAttendance);
		System.out.println(success);

		if (success) {
			redirectAttributes.addAttribute("id", employeeId);
			// redirectAttributes.addAttribute("clockIn", attendanceTime);
			return "redirect:/employeeDetail/{id}";
		} else {
			// 処理失敗時のリダイレクト先などを記述する
			return "error-page";
		}
	}

}