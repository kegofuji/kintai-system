export function formatDate(date, format = 'YYYY-MM-DD') {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');
    
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

export function parseDate(dateString) {
    return new Date(dateString);
}

export function getStartOfMonth(date = new Date()) {
    return new Date(date.getFullYear(), date.getMonth(), 1);
}

export function getEndOfMonth(date = new Date()) {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}

export function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
}

export function subtractDays(date, days) {
    return addDays(date, -days);
}

export function isWeekend(date) {
    const day = date.getDay();
    return day === 0 || day === 6;
}

export function getWorkingDays(startDate, endDate) {
    let count = 0;
    let current = new Date(startDate);
    
    while (current <= endDate) {
        if (!isWeekend(current)) {
            count++;
        }
        current = addDays(current, 1);
    }
    
    return count;
}
