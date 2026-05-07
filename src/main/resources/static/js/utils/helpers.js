function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleTimeString();
}

function getBadgeClass(status) {
    if (!status) return '';
    return status.toLowerCase();
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
