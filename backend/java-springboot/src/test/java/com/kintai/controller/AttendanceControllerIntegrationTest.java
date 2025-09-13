package com.kintai.controller;

import com.kintai.config.TestSecurityConfig;
import com.kintai.dto.ClockRequest;
import com.kintai.dto.common.ApiResponse;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AttendanceController 統合テスト
 * 設計書仕様に基づく勤怠APIの統合テスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.yml")
@Import(TestSecurityConfig.class)
@Transactional
class AttendanceControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    private Employee testEmployee;
    private String authToken;
    
    @BeforeEach
    void setUp() {
        // テスト用社員データ作成
        testEmployee = Employee.builder()
            .employeeCode("TEST001")
            .employeeName("テストユーザー")
            .email("test@example.com")
            .employeePasswordHash("$2a$10$N.zmdr9k7uOCQQydNXty8O8M6LndRLW5wK8XdoJpV9fJ5VKdkXZEm")
            .employeeRole(Employee.EmployeeRole.EMPLOYEE)
            .employmentStatus(Employee.EmploymentStatus.ACTIVE)
            .hiredAt(LocalDate.now())
            .paidLeaveRemainingDays(10)
            .build();
        testEmployee = employeeRepository.save(testEmployee);
        
        // 認証トークン取得（モック）
        authToken = "Bearer mock-jwt-token";
    }
    
    @Test
    @DisplayName("出勤打刻 - 正常ケース")
    void clockIn_Success() {
        ClockRequest request = ClockRequest.builder()
            .employeeId(testEmployee.getEmployeeId())
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity<ClockRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            "/api/attendance/clock-in", HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        
        // データベース確認
        Optional<AttendanceRecord> record = attendanceRecordRepository
            .findByEmployeeIdAndAttendanceDate(testEmployee.getEmployeeId(), LocalDate.now());
        assertThat(record).isPresent();
        assertThat(record.get().getClockInTime()).isNotNull();
        assertThat(record.get().getAttendanceStatus()).isEqualTo(AttendanceRecord.AttendanceStatus.NORMAL);
    }
    
    @Test
    @DisplayName("出勤打刻 - 重複エラー")
    void clockIn_AlreadyClockedIn() {
        // 既存の出勤記録作成
        AttendanceRecord existing = AttendanceRecord.builder()
            .employeeId(testEmployee.getEmployeeId())
            .attendanceDate(LocalDate.now())
            .clockInTime(LocalDateTime.now().minusHours(1))
            .attendanceStatus(AttendanceRecord.AttendanceStatus.NORMAL)
            .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
            .attendanceFixedFlag(false)
            .lateMinutes(0)
            .earlyLeaveMinutes(0)
            .overtimeMinutes(0)
            .nightShiftMinutes(0)
            .build();
        attendanceRecordRepository.save(existing);
        
        ClockRequest request = ClockRequest.builder()
            .employeeId(testEmployee.getEmployeeId())
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity<ClockRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            "/api/attendance/clock-in", HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("ALREADY_CLOCKED_IN");
    }
    
    @Test
    @DisplayName("出勤打刻 - 存在しない社員ID")
    void clockIn_EmployeeNotFound() {
        ClockRequest request = ClockRequest.builder()
            .employeeId(99999L) // 存在しない社員ID
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity<ClockRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            "/api/attendance/clock-in", HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("EMPLOYEE_NOT_FOUND");
    }
    
    @Test
    @DisplayName("退勤打刻 - 正常ケース")
    void clockOut_Success() {
        // 出勤記録を先に作成
        AttendanceRecord attendanceRecord = AttendanceRecord.builder()
            .employeeId(testEmployee.getEmployeeId())
            .attendanceDate(LocalDate.now())
            .clockInTime(LocalDateTime.now().minusHours(8))
            .attendanceStatus(AttendanceRecord.AttendanceStatus.NORMAL)
            .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
            .attendanceFixedFlag(false)
            .lateMinutes(0)
            .earlyLeaveMinutes(0)
            .overtimeMinutes(0)
            .nightShiftMinutes(0)
            .build();
        attendanceRecordRepository.save(attendanceRecord);
        
        ClockRequest request = ClockRequest.builder()
            .employeeId(testEmployee.getEmployeeId())
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity<ClockRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            "/api/attendance/clock-out", HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        
        // データベース確認
        Optional<AttendanceRecord> record = attendanceRecordRepository
            .findByEmployeeIdAndAttendanceDate(testEmployee.getEmployeeId(), LocalDate.now());
        assertThat(record).isPresent();
        assertThat(record.get().getClockOutTime()).isNotNull();
    }
    
    @Test
    @DisplayName("退勤打刻 - 出勤記録なし")
    void clockOut_NoClockInRecord() {
        ClockRequest request = ClockRequest.builder()
            .employeeId(testEmployee.getEmployeeId())
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity<ClockRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            "/api/attendance/clock-out", HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("NOT_CLOCKED_IN");
    }
    
    @Test
    @DisplayName("退勤打刻 - 既に退勤済み")
    void clockOut_AlreadyClockedOut() {
        // 出退勤記録を先に作成
        AttendanceRecord attendanceRecord = AttendanceRecord.builder()
            .employeeId(testEmployee.getEmployeeId())
            .attendanceDate(LocalDate.now())
            .clockInTime(LocalDateTime.now().minusHours(8))
            .clockOutTime(LocalDateTime.now().minusHours(1))
            .attendanceStatus(AttendanceRecord.AttendanceStatus.NORMAL)
            .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
            .attendanceFixedFlag(false)
            .lateMinutes(0)
            .earlyLeaveMinutes(0)
            .overtimeMinutes(0)
            .nightShiftMinutes(0)
            .build();
        attendanceRecordRepository.save(attendanceRecord);
        
        ClockRequest request = ClockRequest.builder()
            .employeeId(testEmployee.getEmployeeId())
            .build();
            
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        HttpEntity<ClockRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            "/api/attendance/clock-out", HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("ALREADY_CLOCKED_OUT");
    }
    
    @Test
    @DisplayName("勤怠履歴取得 - 正常ケース")
    void getHistory_Success() {
        // テスト用勤怠記録作成
        AttendanceRecord record1 = AttendanceRecord.builder()
            .employeeId(testEmployee.getEmployeeId())
            .attendanceDate(LocalDate.now().minusDays(1))
            .clockInTime(LocalDateTime.now().minusDays(1).withHour(9).withMinute(0))
            .clockOutTime(LocalDateTime.now().minusDays(1).withHour(18).withMinute(0))
            .attendanceStatus(AttendanceRecord.AttendanceStatus.NORMAL)
            .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
            .attendanceFixedFlag(false)
            .lateMinutes(0)
            .earlyLeaveMinutes(0)
            .overtimeMinutes(0)
            .nightShiftMinutes(0)
            .build();
        attendanceRecordRepository.save(record1);
        
        AttendanceRecord record2 = AttendanceRecord.builder()
            .employeeId(testEmployee.getEmployeeId())
            .attendanceDate(LocalDate.now())
            .clockInTime(LocalDateTime.now().withHour(9).withMinute(5))
            .attendanceStatus(AttendanceRecord.AttendanceStatus.NORMAL)
            .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
            .attendanceFixedFlag(false)
            .lateMinutes(5)
            .earlyLeaveMinutes(0)
            .overtimeMinutes(0)
            .nightShiftMinutes(0)
            .build();
        attendanceRecordRepository.save(record2);
        
        String url = String.format("/api/attendance/history?employeeId=%d", testEmployee.getEmployeeId());
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            url, HttpMethod.GET, null, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }
    
    @Test
    @DisplayName("勤怠履歴取得 - 存在しない社員ID")
    void getHistory_EmployeeNotFound() {
        String url = "/api/attendance/history?employeeId=99999";
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            url, HttpMethod.GET, null, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("EMPLOYEE_NOT_FOUND");
    }
    
    @Test
    @DisplayName("月末勤怠申請 - 正常ケース")
    void submitMonthly_Success() {
        // テスト用勤怠記録作成（8月分）
        AttendanceRecord record = AttendanceRecord.builder()
            .employeeId(testEmployee.getEmployeeId())
            .attendanceDate(LocalDate.of(2025, 8, 15))
            .clockInTime(LocalDateTime.of(2025, 8, 15, 9, 0))
            .clockOutTime(LocalDateTime.of(2025, 8, 15, 18, 0))
            .attendanceStatus(AttendanceRecord.AttendanceStatus.NORMAL)
            .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
            .attendanceFixedFlag(false)
            .lateMinutes(0)
            .earlyLeaveMinutes(0)
            .overtimeMinutes(0)
            .nightShiftMinutes(0)
            .build();
        attendanceRecordRepository.save(record);
        
        String requestBody = String.format(
            "{\"employeeId\": %d, \"targetMonth\": \"2025-08\"}", 
            testEmployee.getEmployeeId()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", authToken);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            "/api/attendance/monthly-submit", HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
    }
    
    @Test
    @DisplayName("月末勤怠申請 - 存在しない社員ID")
    void submitMonthly_EmployeeNotFound() {
        String requestBody = "{\"employeeId\": 99999, \"targetMonth\": \"2025-08\"}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", authToken);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
            "/api/attendance/monthly-submit", HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<Object>>() {});
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("EMPLOYEE_NOT_FOUND");
    }
}
