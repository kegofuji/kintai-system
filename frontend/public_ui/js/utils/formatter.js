export function formatCurrency(amount) {
    return new Intl.NumberFormat('ja-JP', {
        style: 'currency',
        currency: 'JPY'
    }).format(amount);
}

export function formatNumber(number, minimumFractionDigits = 0, maximumFractionDigits = 2) {
    return new Intl.NumberFormat('ja-JP', {
        minimumFractionDigits,
        maximumFractionDigits
    }).format(number);
}

export function formatTime(minutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
}

export function formatDuration(minutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}時間${mins}分`;
}

export function formatStatus(status) {
    const statusMap = {
        'PENDING': '審査中',
        'APPROVED': '承認済',
        'REJECTED': '却下',
        'WORKING': '勤務中',
        'ABSENT': '欠勤',
        'ON_LEAVE': '休暇中',
        'DONE': '完了'
    };
    
    return statusMap[status] || status;
}

export function formatName(lastName, firstName) {
    return `${lastName} ${firstName}`;
}

export function formatPhoneNumber(phone) {
    return phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
}

export function formatPostalCode(code) {
    return code.replace(/(\d{3})(\d{4})/, '$1-$2');
}

export function formatEmployeeId(id) {
    return String(id).padStart(6, '0');
}
