package com.kintai.service;

import com.kintai.entity.AdjustmentRequest;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.LeaveRequest;
import com.kintai.entity.Employee;
import com.kintai.repository.AdjustmentRequestRepository;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.LeaveRequestRepository;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.TimeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation