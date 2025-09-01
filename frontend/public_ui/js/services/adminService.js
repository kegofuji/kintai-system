import { API_BASE_URL } from '../constants/config.js';

export const adminService = {
    async getAllEmployees() {
        try {
            const response = await fetch(`${API_BASE_URL}/admin/employees`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
            });
            return await response.json();
        } catch (error) {
            console.error('Get employees error:', error);
            throw error;
        }
    },

    async getPendingRequests() {
        try {
            const response = await fetch(`${API_BASE_URL}/admin/requests/pending`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
            });
            return await response.json();
        } catch (error) {
            console.error('Get pending requests error:', error);
            throw error;
        }
    },

    async updateLeaveRequestStatus(requestId, status) {
        try {
            const response = await fetch(`${API_BASE_URL}/admin/requests/leave/${requestId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
                body: JSON.stringify({ status }),
            });
            return await response.json();
        } catch (error) {
            console.error('Update leave request status error:', error);
            throw error;
        }
    },

    async updateAdjustmentRequestStatus(requestId, status) {
        try {
            const response = await fetch(`${API_BASE_URL}/admin/requests/adjustment/${requestId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`,
                },
                body: JSON.stringify({ status }),
            });
            return await response.json();
        } catch (error) {
            console.error('Update adjustment request status error:', error);
            throw error;
        }
    }
};

export function setupAdminListeners() {
    // 管理者画面の初期化
    const adminPanel = document.querySelector('#admin-panel');
    if (adminPanel) {
        loadAdminData();
    }
}

async function loadAdminData() {
    try {
        const [employees, pendingRequests] = await Promise.all([
            adminService.getAllEmployees(),
            adminService.getPendingRequests()
        ]);
        
        // 従業員リストを表示
        displayEmployees(employees);
        
        // 保留中のリクエストを表示
        displayPendingRequests(pendingRequests);
    } catch (error) {
        console.error('Admin data loading failed:', error);
    }
}

function displayEmployees(employees) {
    const employeeList = document.querySelector('#employee-list');
    if (employeeList) {
        employeeList.innerHTML = employees.map(employee => `
            <tr>
                <td>${employee.id}</td>
                <td>${employee.name}</td>
                <td>${employee.department}</td>
                <td>
                    <button onclick="viewEmployeeDetails(${employee.id})">詳細</button>
                </td>
            </tr>
        `).join('');
    }
}

function displayPendingRequests(requests) {
    const requestList = document.querySelector('#pending-request-list');
    if (requestList) {
        requestList.innerHTML = requests.map(request => `
            <tr>
                <td>${request.id}</td>
                <td>${request.employeeName}</td>
                <td>${request.type}</td>
                <td>${request.requestDate}</td>
                <td>
                    <button onclick="approveRequest(${request.id}, '${request.type}')">承認</button>
                    <button onclick="rejectRequest(${request.id}, '${request.type}')">却下</button>
                </td>
            </tr>
        `).join('');
    }
}
