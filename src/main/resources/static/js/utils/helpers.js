function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleTimeString();
}

function getBadgeClass(status) {
    if (!status) return '';
    return status.toLowerCase();
}
