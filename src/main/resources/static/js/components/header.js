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

            // Show new batch button if has MANAGE_BATCH
            if (hasAuthority('MANAGE_BATCH') || hasAuthority('ROLE_ADMIN')) {
                const newBatchBtn = document.getElementById('btn-new-batch');
                if (newBatchBtn) newBatchBtn.classList.remove('hidden');
                
                const fabNewBatch = document.getElementById('fab-new-batch');
                if (fabNewBatch) {
                    fabNewBatch.classList.remove('hidden');
                    fabNewBatch.addEventListener('click', () => {
                        if (newBatchBtn) newBatchBtn.click();
                    });
                }
            }

            // Show admin nav if has ROLE_ADMIN
            if (hasAuthority('ROLE_ADMIN')) {
                const adminItem = document.getElementById('nav-admin-item');
                if (adminItem) adminItem.classList.remove('hidden');
                const mobileAdminItem = document.getElementById('mobile-nav-system');
                if (mobileAdminItem) mobileAdminItem.style.display = 'flex';
            }

            // Show history nav if has VIEW_HISTORY or ROLE_ADMIN
            if (hasAuthority('VIEW_HISTORY') || hasAuthority('ROLE_ADMIN')) {
                const historyItem = document.getElementById('nav-history-item');
                if (historyItem) {
                    historyItem.classList.remove('hidden');
                }
                const mobileHistoryItem = document.getElementById('mobile-nav-history');
                if (mobileHistoryItem) mobileHistoryItem.style.display = 'flex';
            }
        }
    } catch (e) {
        console.error('Không thể giải mã token', e);
    }

    // Topbar User Avatar Dropdown
    const userDropdownWrapper = document.getElementById('user-info');
    if (userDropdownWrapper) {
        userDropdownWrapper.addEventListener('click', (e) => {
            if (e.target.closest('#topbar-btn-logout')) return; // handled separately
            userDropdownWrapper.classList.toggle('active');
        });
        
        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!userDropdownWrapper.contains(e.target)) {
                userDropdownWrapper.classList.remove('active');
            }
        });
    }

    // Logout
    const btnLogout = document.getElementById('btn-logout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('user');
            window.location.href = '/login.html';
        });
    }
    
    // Topbar Logout
    const topbarBtnLogout = document.getElementById('topbar-btn-logout');
    if (topbarBtnLogout) {
        topbarBtnLogout.addEventListener('click', () => {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('user');
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
