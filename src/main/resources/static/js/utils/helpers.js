function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    
    return `${hours}:${minutes}:${seconds}\n${day}/${month}/${year}`;
}

function formatTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${hours}:${minutes}:${seconds}`;
}

function getBadgeClass(status) {
    if (!status) return '';
    return status.toLowerCase();
}

function getLabelVietnamese(label) {
    if (!label) return '';
    const map = {
        'ripe': 'Chín',
        'unripe': 'Chưa chín',
        'reject': 'Loại bỏ',
        'green': 'Chưa chín',
        'tomato': 'Cà chua'
    };
    return map[label.toLowerCase()] || label;
}

function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Lỗi khi giải mã JWT:', e);
        return null;
    }
}

function hasAuthority(authority) {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;
    
    const payload = parseJwt(token);
    if (payload && payload.authorities && Array.isArray(payload.authorities)) {
        return payload.authorities.includes(authority);
    }

    return false;
}
