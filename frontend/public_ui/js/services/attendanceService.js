import { API_BASE_URL } from '../constants/config.js';

export const attendanceService = {
    async checkIn(employeeId) {
        try {
            const response = await fetch(`${API_BASE_URL}/attendance/check-in/${employeeId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
            });
            return await response.json();
        } catch (error) {
            console.error('Check-in error:', error);
            throw error;
        }
    },

    async checkOut(employeeId) {
        try {
            const response = await fetch(`${API_BASE_URL}/attendance/check-out/${employeeId}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
            });
            return await response.json();
        } catch (error) {
            console.error('Check-out error:', error);
            throw error;
        }
    },

    async getAttendanceRecords(employeeId, startDate, endDate) {
        try {
            const params = new URLSearchParams();
            if (startDate) params.append('startDate', startDate);
            if (endDate) params.append('endDate', endDate);

            const response = await fetch(
                `${API_BASE_URL}/attendance/records/${employeeId}?${params}`,
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`,
                    },
                }
            );
            return await response.json();
        } catch (error) {
            console.error('Get attendance records error:', error);
            throw error;
        }
    }
};

export function setupAttendanceListeners() {
    const checkInButton = document.querySelector('#check-in-button');
    const checkOutButton = document.querySelector('#check-out-button');

    if (checkInButton) {
        checkInButton.addEventListener('click', async () => {
            try {
                const employeeId = localStorage.getItem('employeeId');
                await attendanceService.checkIn(employeeId);
                // 成功メッセージを表示
            } catch (error) {
                console.error('Check-in failed:', error);
            }
        });
    }

    if (checkOutButton) {
        checkOutButton.addEventListener('click', async () => {
            try {
                const employeeId = localStorage.getItem('employeeId');
                await attendanceService.checkOut(employeeId);
                // 成功メッセージを表示
            } catch (error) {
                console.error('Check-out failed:', error);
            }
        });
    }
}
