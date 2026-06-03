/**
 * Header / Sidebar initialization for pages with sidebar layout.
 * Sets up user info in top bar, admin nav visibility, logout, and new batch button.
 */
function initSidebar() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    try {
        const payload = typeof parseJwt === 'function' ? parseJwt(token) : JSON.parse(atob(token.split('.')[1]));
        if (payload) {
            const username = payload.sub || 'Người dùng';
            
            // Update user display name
            const nameEl = document.getElementById('user-display-name');
            if (nameEl) nameEl.textContent = username;

            // Update avatar
            const avatarEl = document.getElementById('user-avatar');
            if (avatarEl) avatarEl.textContent = username.charAt(0).toUpperCase();

            // Show admin nav if has ROLE_ADMIN
            if (hasAuthority('ROLE_ADMIN')) {
                const adminItem = document.getElementById('nav-admin-item');
                if (adminItem) adminItem.classList.remove('hidden');
            }

            // Show history nav if has VIEW_HISTORY or ROLE_ADMIN
            if (hasAuthority('VIEW_HISTORY') || hasAuthority('ROLE_ADMIN')) {
                const historyItem = document.getElementById('nav-history-item');
                if (historyItem) {
                    historyItem.classList.remove('hidden');
                }
            }
        }
    } catch (e) {
        console.error('Không thể giải mã token', e);
    }

    // Logout button
    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('accessToken');
            window.location.href = '/login.html';
        });
    }

    // New batch button visibility (only if has permission)
    const btnNewBatch = document.getElementById('btn-new-batch');
    if (btnNewBatch) {
        if (hasAuthority('MANAGE_BATCH') || hasAuthority('ROLE_ADMIN')) {
            btnNewBatch.classList.remove('hidden');
        }
    }
}

// Legacy function - kept for backward compatibility but now a no-op
function renderHeader(currentPath, currentBatchName) {
    // Sidebar is now static in HTML; this function initializes sidebar state
    initSidebar();
}
