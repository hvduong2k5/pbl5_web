function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleTimeString();
}

function getBadgeClass(status) {
    if (!status) return '';
    return status.toLowerCase();
}

function hasAuthority(authority) {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;
    try {
        const payloadStr = atob(token.split('.')[1]);
        const payload = JSON.parse(payloadStr);
        if (payload.authorities && Array.isArray(payload.authorities)) {
            return payload.authorities.includes(authority);
        }
    } catch (e) {
        console.error('Lỗi khi đọc token authorities:', e);
    }
    return false;
}
