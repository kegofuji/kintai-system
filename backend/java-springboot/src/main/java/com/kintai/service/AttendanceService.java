package com.kintai.service;

import com.kintai.entity.AttendanceRecord;
import com.kintai.exception.BusinessException;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.DateUtil;
import com.kintai.util.TimeCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private TimeCalculator timeCalculator;
    
    @Autowired
    private DateUtil dateUtil;
    
    /**
     * 出勤打刻
     * @param employeeId 社員ID
     * @return 出勤記録
     */
    @Transactional
    public AttendanceRecord clockIn(Long employeeId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        
        try {
            // 社員存在確認
            if (!employeeRepository.existsById(employeeId)) {
                throw new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            // 既存の出勤チェック
            Optional<AttendanceRecord> existingRecord = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);
            
            if (existingRecord.isPresent() && existingRecord.get().getClockInTime() != null) {
                throw new BusinessException("ALREADY_CLOCKED_IN", "本日は既に出勤打刻済みです");
            }
            
            AttendanceRecord record;
            if (existingRecord.isPresent()) {
                record = existingRecord.get();
            } else {
                record = new AttendanceRecord(employeeId, today);
            }
            
            // 確定済みデータのチェック
            if (record.getAttendanceFixedFlag()) {
                throw new BusinessException("FIXED_ATTENDANCE", "確定済みのため変更できません");
            }
            
            record.setClockInTime(now);
            
            // 遅刻時間計算
            int lateMinutes = timeCalculator.calculateLateMinutes(now);
            record.setLateMinutes(lateMinutes);
            
            record = attendanceRecordRepository.save(record);
            logger.info("Clock in successful. Employee: {}, Time: {}, Late: {} min", 
                       employeeId, now, lateMinutes);
            
            return record;
            
        } catch (BusinessException e) {
            

