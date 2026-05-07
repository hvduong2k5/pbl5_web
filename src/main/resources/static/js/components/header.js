function renderHeader(currentPath, currentBatchName) {
    const container = document.getElementById('header-container');
    if (!container) return;
    
    const isHome = currentPath === 'home' || currentPath === '/' || currentPath.endsWith('index.html');
    const isHistory = currentPath.endsWith('history.html');
    const isAdmin = currentPath.endsWith('admin.html');
    const isLogin = currentPath.endsWith('login.html');
    
    const token = localStorage.getItem('accessToken');
    let userMenuHtml = '';
    let adminLinkHtml = '';
    
    if (token && !isLogin) {
        try {
            // Giải mã JWT để lấy username và authorities thông qua hàm an toàn parseJwt trong helpers.js
            const payload = typeof parseJwt === 'function' ? parseJwt(token) : JSON.parse(atob(token.split('.')[1]));
            if (payload) {
                const username = payload.sub || 'Tài khoản';
                const authorities = payload.authorities || [];
                
                // Tìm role chính (bắt đầu bằng ROLE_)
                let mainRole = 'USER';
                const roleAuth = authorities.find(a => a.startsWith('ROLE_'));
                if (roleAuth) {
                    mainRole = roleAuth.substring(5); // Cắt bỏ chữ 'ROLE_'
                }
                
                userMenuHtml = `
                    <div class="user-menu" style="margin-left: 15px;">
                        <button id="user-menu-btn" class="user-btn">
                            <span>${username}</span>
                            <span style="font-size: 0.8em;">▼</span>
                        </button>
                        <div id="user-dropdown" class="dropdown-content">
                            <div class="dropdown-header">
                                <span class="username">${username}</span>
                                <span class="role-badge">${mainRole}</span>
                            </div>
                            <a href="#" id="menu-settings">Cài đặt (Đổi mật khẩu)</a>
                            <a href="#" id="menu-logout" class="logout-btn">Đăng xuất</a>
                        </div>
                    </div>
                `;
                
                if (hasAuthority('ROLE_ADMIN')) {
                    adminLinkHtml = `<a href="/admin.html" class="${isAdmin ? 'active' : ''}">Admin Console</a>`;
                }
            }
        } catch (e) {
            console.error('Không thể giải mã token', e);
        }
    }
    
    container.innerHTML = `
        <header class="app-header">
            <div class="nav-links">
                <a href="/" class="${isHome ? 'active' : ''}">Home</a>
                <a href="/history.html" class="${isHistory ? 'active' : ''}">History</a>
                ${adminLinkHtml}
            </div>
            <div class="header-right">
                <div class="current-batch">
                    ${currentBatchName ? `Current Batch: ${currentBatchName}` : 'No Active Batch'}
                </div>
                ${userMenuHtml}
            </div>
        </header>
    `;
    
    // Gắn sự kiện cho User Menu
    if (token && !isLogin) {
        const userBtn = document.getElementById('user-menu-btn');
        const dropdown = document.getElementById('user-dropdown');
        
        if (userBtn && dropdown) {
            userBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                dropdown.classList.toggle('show');
            });
            
            // Đóng dropdown khi click ra ngoài
            window.addEventListener('click', (e) => {
                if (!userBtn.contains(e.target) && !dropdown.contains(e.target)) {
                    dropdown.classList.remove('show');
                }
            });
            
            // Xử lý Cài đặt
            document.getElementById('menu-settings').addEventListener('click', (e) => {
                e.preventDefault();
                alert('Tính năng đổi mật khẩu đang được phát triển!');
                dropdown.classList.remove('show');
            });
            
            // Xử lý Đăng xuất
            document.getElementById('menu-logout').addEventListener('click', (e) => {
                e.preventDefault();
                localStorage.removeItem('accessToken');
                window.location.href = '/login.html';
            });
        }
    }
}
