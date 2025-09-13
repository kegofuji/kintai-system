// 従業員管理コンポーネント
class EmployeeManagementComponent {
    constructor() {
        this.adminService = new AdminService();
    }

    render() {
        return `
            <div class="container mt-4">
                <h2>従業員管理</h2>
                <div class="employee-table">
                    <p>従業員データを読み込み中...</p>
                </div>
            </div>
        `;
    }
}

window.EmployeeManagementComponent = EmployeeManagementComponent;
