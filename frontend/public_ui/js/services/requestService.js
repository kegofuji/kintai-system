import { API_BASE_URL } from '../constants/config.js';

export const requestService = {
    async createLeaveRequest(requestData) {
        try {
            const response = await fetch(`${API_BASE_URL}/requests/leave`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
                body: JSON.stringify(requestData),
            });
            return await response.json();
        } catch (error) {
            console.error('Create leave request error:', error);
            throw error;
        }
    },

    async createAdjustmentRequest(requestData) {
        try {
            const response = await fetch(`${API_BASE_URL}/requests/adjustment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
                body: JSON.stringify(requestData),
            });
            return await response.json();
        } catch (error) {
            console.error('Create adjustment request error:', error);
            throw error;
        }
    },

    async getLeaveRequests(employeeId) {
        try {
            const response = await fetch(`${API_BASE_URL}/requests/leave/${employeeId}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
            });
            return await response.json();
        } catch (error) {
            console.error('Get leave requests error:', error);
            throw error;
        }
    },

    async getAdjustmentRequests(employeeId) {
        try {
            const response = await fetch(`${API_BASE_URL}/requests/adjustment/${employeeId}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
            });
            return await response.json();
        } catch (error) {
            console.error('Get adjustment requests error:', error);
            throw error;
        }
    }
};

export function setupRequestListeners() {
    const leaveRequestForm = document.querySelector('#leave-request-form');
    const adjustmentRequestForm = document.querySelector('#adjustment-request-form');

    if (leaveRequestForm) {
        leaveRequestForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(leaveRequestForm);
            const requestData = {
                startDate: formData.get('startDate'),
                endDate: formData.get('endDate'),
                reason: formData.get('reason'),
            };
            try {
                await requestService.createLeaveRequest(requestData);
                // 成功メッセージを表示
            } catch (error) {
                console.error('Leave request failed:', error);
            }
        });
    }

    if (adjustmentRequestForm) {
        adjustmentRequestForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(adjustmentRequestForm);
            const requestData = {
                date: formData.get('date'),
                reason: formData.get('reason'),
            };
            try {
                await requestService.createAdjustmentRequest(requestData);
                // 成功メッセージを表示
            } catch (error) {
                console.error('Adjustment request failed:', error);
            }
        });
    }
}
