let currentUserId = null;
let currentRoleId = null;
let allPermissions = [];

let userCurrentPage = 0;
let userTotalPages = 1;
const PAGE_SIZE = 10;

let roleCurrentPage = 0;
let roleTotalPages = 1;

document.addEventListener('DOMContentLoaded', async () => {
    // Chỉ có admin mới được vào trang này
    if (!hasAuthority('ROLE_ADMIN')) {
        alert('Bạn không có quyền truy cập trang này.');
        window.location.href = '/';
        return;
    }

    initSidebar();

    // Bind events
    document.getElementById('btn-add-user').addEventListener('click', openAddUserModal);
    document.getElementById('btn-add-role').addEventListener('click', openAddRoleModal);
    
    document.getElementById('user-form').addEventListener('submit', handleUserSubmit);
    document.getElementById('role-form').addEventListener('submit', handleRoleSubmit);

    document.getElementById('btn-user-search').addEventListener('click', () => {
        userCurrentPage = 0;
        loadUsers();
    });
    
    document.getElementById('user-search-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            userCurrentPage = 0;
            loadUsers();
        }
    });

    document.getElementById('btn-user-prev').addEventListener('click', () => {
        if (userCurrentPage > 0) {
            userCurrentPage--;
            loadUsers();
        }
    });

    document.getElementById('btn-user-next').addEventListener('click', () => {
        if (userCurrentPage < userTotalPages - 1) {
            userCurrentPage++;
            loadUsers();
        }
    });

    document.getElementById('btn-role-prev').addEventListener('click', () => {
        if (roleCurrentPage > 0) {
            roleCurrentPage--;
            loadRoles();
        }
    });

    document.getElementById('btn-role-next').addEventListener('click', () => {
        if (roleCurrentPage < roleTotalPages - 1) {
            roleCurrentPage++;
            loadRoles();
        }
    });

    // Initial data load
    await loadAllPermissions();
    loadUsers();
    loadRoles();
});

// --- User Management ---

async function loadUsers() {
    try {
        const keyword = document.getElementById('user-search-input').value.trim();
        const response = await API.getAllUsers(keyword, userCurrentPage, PAGE_SIZE);
        
        const tbody = document.getElementById('user-body');
        tbody.innerHTML = '';
        
        const mobileContainer = document.getElementById('mobile-users-cards');
        if (mobileContainer) mobileContainer.innerHTML = '';
        
        userTotalPages = response.totalPages || 1;
        document.getElementById('user-page-info').textContent = `Trang ${userCurrentPage + 1} / ${userTotalPages}`;
        
        document.getElementById('btn-user-prev').disabled = userCurrentPage === 0;
        document.getElementById('btn-user-next').disabled = userCurrentPage >= userTotalPages - 1;

        if (!response.content || response.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding: 20px; color: var(--text-muted);">Không tìm thấy người dùng</td></tr>`;
            if (mobileContainer) mobileContainer.innerHTML = `<div style="text-align:center; padding: 20px; color: var(--text-muted);">Không tìm thấy người dùng</div>`;
            return;
        }

        response.content.forEach(user => {
            const tr = document.createElement('tr');
            
            const rolesHtml = user.roles.map(r => {
                const roleName = typeof r === 'string' ? r : r.name;
                let badgeClass = 'role-default';
                if (roleName === 'ADMIN') badgeClass = 'role-admin';
                else if (roleName === 'OPERATOR') badgeClass = 'role-operator';
                return `<span class="badge ${badgeClass}" style="margin-right: 4px; margin-bottom: 4px;">${roleName}</span>`;
            }).join('');
            
            const statusHtml = user.enabled 
                ? `<span class="badge active-status">Hoạt động</span>` 
                : `<span class="badge disabled-status">Vô hiệu hóa</span>`;
            
            tr.innerHTML = `
                <td style="font-weight:600;">#${user.id}</td>
                <td>
                    <div class="user-cell">
                        <div class="user-avatar-circle">${user.username.substring(0, 2).toUpperCase()}</div>
                        <div class="user-name-text">${user.username}</div>
                    </div>
                </td>
                <td>${statusHtml}</td>
                <td style="max-width: 200px; display:flex; flex-wrap:wrap; gap:4px; align-items:center;">${rolesHtml}</td>
                <td>
                    <div class="action-btns">
                        <button class="btn btn-outline btn-icon" onclick='openEditUserModal(${JSON.stringify(user).replace(/'/g, "&apos;")})' title="Sửa">✏️</button>
                        <button class="btn btn-danger btn-icon" onclick="deleteUser(${user.id})" title="Xóa">🗑️</button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);

            if (mobileContainer) {
                const cardHtml = `
                    <div class="list-card">
                        <div class="card-title-row" style="margin-bottom: 12px; display:flex; gap:12px; align-items:center;">
                            <div class="user-avatar-circle" style="width: 50px; height: 50px; font-size: 1.2em; flex-shrink: 0;">${user.username.substring(0, 2).toUpperCase()}</div>
                            <div style="flex:1;">
                                <div class="user-name-text">${user.username}</div>
                                <div style="font-size:0.85em; color:var(--text-muted);">${user.email || 'No email'}</div>
                            </div>
                            <div class="action-btns">
                                <button class="btn btn-outline btn-icon" onclick='openEditUserModal(${JSON.stringify(user).replace(/'/g, "&apos;")})'>✏️</button>
                                <button class="btn btn-danger btn-icon" onclick="deleteUser(${user.id})">🗑️</button>
                            </div>
                        </div>
                        <div class="card-details-row" style="margin-top: 4px; justify-content: space-between;">
                            <div style="display:flex; gap:4px; flex-wrap:wrap;">${rolesHtml}</div>
                            <div>${statusHtml}</div>
                        </div>
                    </div>
                `;
                mobileContainer.insertAdjacentHTML('beforeend', cardHtml);
            }
        });
    } catch (e) {
        console.error('Lỗi khi load users:', e);
        alert('Lỗi tải danh sách người dùng: ' + e.message);
    }
}

async function loadRoles() {
    try {
        const response = await API.getAllRoles(roleCurrentPage, PAGE_SIZE);
        const tbody = document.getElementById('role-body');
        tbody.innerHTML = '';

        const mobileContainer = document.getElementById('mobile-roles-cards');
        if (mobileContainer) mobileContainer.innerHTML = '';

        roleTotalPages = response.totalPages || 1;
        document.getElementById('role-page-info').textContent = `Trang ${roleCurrentPage + 1} / ${roleTotalPages}`;
        
        document.getElementById('btn-role-prev').disabled = roleCurrentPage === 0;
        document.getElementById('btn-role-next').disabled = roleCurrentPage >= roleTotalPages - 1;

        if (!response.content || response.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding: 20px; color: var(--text-muted);">Chưa có vai trò nào</td></tr>`;
            if (mobileContainer) mobileContainer.innerHTML = `<div style="text-align:center; padding: 20px; color: var(--text-muted);">Chưa có vai trò nào</div>`;
            return;
        }

        response.content.forEach(role => {
            const tr = document.createElement('tr');
            
            const permsHtml = role.permissions.map(p => 
                `<span class="badge perm-badge" style="margin-right: 4px; margin-bottom: 4px; font-size: 0.7em;">${p.name}</span>`
            ).join('');
            
            tr.innerHTML = `
                <td style="font-weight:600;">#${role.id}</td>
                <td class="role-name-cell">${role.name}</td>
                <td style="max-width: 300px; display:flex; flex-wrap:wrap; gap:4px;">${permsHtml}</td>
                <td>
                    <div class="action-btns">
                        <button class="btn btn-outline btn-icon" onclick='openEditRoleModal(${JSON.stringify(role).replace(/'/g, "&apos;")})' title="Sửa">✏️</button>
                        <button class="btn btn-danger btn-icon" onclick="deleteRole(${role.id})" title="Xóa">🗑️</button>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);

            if (mobileContainer) {
                const cardHtml = `
                    <div class="list-card">
                        <div class="card-title-row" style="margin-bottom: 12px; display:flex; justify-content: space-between; align-items:center;">
                            <div class="user-name-text" style="font-size: 1.1em; color: var(--primary-color);">${role.name}</div>
                            <div class="action-btns">
                                <button class="btn btn-outline btn-icon" onclick='openEditRoleModal(${JSON.stringify(role).replace(/'/g, "&apos;")})'>✏️</button>
                                <button class="btn btn-danger btn-icon" onclick="deleteRole(${role.id})">🗑️</button>
                            </div>
                        </div>
                        <div class="card-details-row" style="margin-top: 4px; display:flex; flex-wrap:wrap; gap:4px;">
                            ${permsHtml}
                        </div>
                    </div>
                `;
                mobileContainer.insertAdjacentHTML('beforeend', cardHtml);
            }
        });
    } catch (e) {
        console.error('Lỗi khi load roles:', e);
    }
}

async function loadAllPermissions() {
    try {
        allPermissions = await API.getAllPermissions();
    } catch (e) {
        console.error('Lỗi load permissions:', e);
    }
}

// --- User Actions ---

async function openAddUserModal() {
    currentUserId = null;
    document.getElementById('user-modal-title').textContent = 'Thêm người dùng mới';
    document.getElementById('user-form').reset();
    document.getElementById('userId').value = '';
    document.getElementById('user-error').classList.add('hidden');
    document.getElementById('username').readOnly = false;
    document.getElementById('password').required = true;
    
    await populateRoleCheckboxes([]);
    
    document.getElementById('user-modal').classList.remove('hidden');
}

async function openEditUserModal(user) {
    currentUserId = user.id;
    document.getElementById('user-modal-title').textContent = 'Cập nhật người dùng';
    document.getElementById('userId').value = user.id;
    document.getElementById('username').value = user.username;
    document.getElementById('username').readOnly = true; 
    document.getElementById('password').value = ''; 
    document.getElementById('password').required = false; 
    document.getElementById('enabled').checked = user.enabled;
    document.getElementById('user-error').classList.add('hidden');
    
    const userRoleNames = user.roles.map(r => typeof r === 'string' ? r : r.name);
    await populateRoleCheckboxes(userRoleNames);
    
    document.getElementById('user-modal').classList.remove('hidden');
}

async function populateRoleCheckboxes(checkedRoleNames) {
    const container = document.getElementById('role-checkboxes');
    container.innerHTML = 'Đang tải...';
    try {
        const response = await API.getAllRoles(0, 100); 
        const roles = response.content || [];
        
        container.innerHTML = '';
        if (roles.length === 0) {
            container.innerHTML = '<span style="color:var(--text-muted); font-size:0.9em;">Chưa có vai trò nào trong hệ thống.</span>';
            return;
        }

        roles.forEach(role => {
            const div = document.createElement('div');
            div.className = 'checkbox-item';
            const isChecked = checkedRoleNames.includes(role.name) ? 'checked' : '';
            div.innerHTML = `
                <input type="checkbox" id="role_${role.name}" name="roles" value="${role.name}" ${isChecked} />
                <label for="role_${role.name}">${role.name}</label>
            `;
            container.appendChild(div);
        });
    } catch (e) {
        container.innerHTML = '<span style="color:var(--danger-color); font-size:0.9em;">Lỗi tải vai trò</span>';
    }
}

async function handleUserSubmit(e) {
    e.preventDefault();
    const errorEl = document.getElementById('user-error');
    errorEl.classList.add('hidden');
    
    const id = document.getElementById('userId').value;
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const enabled = document.getElementById('enabled').checked;
    
    const roleCheckboxes = document.querySelectorAll('input[name="roles"]:checked');
    const roles = Array.from(roleCheckboxes).map(cb => cb.value);
    
    if (username.length < 3) {
        errorEl.textContent = 'Tên đăng nhập phải có ít nhất 3 ký tự.';
        errorEl.classList.remove('hidden');
        return;
    }
    
    const dto = { username, enabled };
    if (password) {
        if (password.length < 6) {
            errorEl.textContent = 'Mật khẩu phải có ít nhất 6 ký tự.';
            errorEl.classList.remove('hidden');
            return;
        }
        dto.password = password;
    }
    
    try {
        if (id) {
            // Update
            await API.updateUser(id, dto);
            if (roles.length >= 0) {
                await API.updateUserRoles(id, roles);
            }
            alert('Cập nhật người dùng thành công.');
        } else {
            // Create
            if (!password) {
                errorEl.textContent = 'Mật khẩu là bắt buộc khi tạo người dùng mới.';
                errorEl.classList.remove('hidden');
                return;
            }
            dto.roles = roles; 
            await API.createUser(dto);
            alert('Tạo người dùng thành công.');
        }
        closeModal('user-modal');
        loadUsers();
    } catch (err) {
        errorEl.textContent = err.message;
        errorEl.classList.remove('hidden');
    }
}

async function deleteUser(id) {
    if (confirm('Bạn có chắc chắn muốn xóa người dùng này?')) {
        try {
            await API.deleteUser(id);
            alert('Đã xóa người dùng.');
            loadUsers();
        } catch (e) {
            alert('Lỗi: ' + e.message);
        }
    }
}

// --- Role Actions ---

function openAddRoleModal() {
    currentRoleId = null;
    document.getElementById('role-modal-title').textContent = 'Tạo vai trò mới';
    document.getElementById('role-form').reset();
    document.getElementById('roleId').value = '';
    document.getElementById('roleName').readOnly = false;
    document.getElementById('role-error').classList.add('hidden');
    
    populatePermissionCheckboxes([]);
    document.getElementById('role-modal').classList.remove('hidden');
}

function openEditRoleModal(role) {
    currentRoleId = role.id;
    document.getElementById('role-modal-title').textContent = 'Cập nhật vai trò';
    document.getElementById('roleId').value = role.id;
    document.getElementById('roleName').value = role.name;
    document.getElementById('roleName').readOnly = true; 
    document.getElementById('role-error').classList.add('hidden');
    
    const permNames = role.permissions.map(p => p.name);
    populatePermissionCheckboxes(permNames);
    
    document.getElementById('role-modal').classList.remove('hidden');
}

function populatePermissionCheckboxes(checkedPermNames) {
    const container = document.getElementById('permission-checkboxes');
    container.innerHTML = '';
    
    if (allPermissions.length === 0) {
        container.innerHTML = '<span style="color:var(--text-muted); font-size:0.9em;">Không có quyền hạn nào khả dụng.</span>';
        return;
    }

    allPermissions.forEach(perm => {
        const div = document.createElement('div');
        div.className = 'checkbox-item';
        const isChecked = checkedPermNames.includes(perm.name) ? 'checked' : '';
        div.innerHTML = `
            <input type="checkbox" id="perm_${perm.id}" name="permissions" value="${perm.id}" ${isChecked} />
            <label for="perm_${perm.id}">${perm.name}</label>
        `;
        container.appendChild(div);
    });
}

async function handleRoleSubmit(e) {
    e.preventDefault();
    const errorEl = document.getElementById('role-error');
    errorEl.classList.add('hidden');
    
    const id = document.getElementById('roleId').value;
    const name = document.getElementById('roleName').value.trim().toUpperCase();
    
    const permCheckboxes = document.querySelectorAll('input[name="permissions"]:checked');
    const permissionIds = Array.from(permCheckboxes).map(cb => parseInt(cb.value, 10));
    
    if (!name) {
        errorEl.textContent = 'Tên vai trò không được để trống.';
        errorEl.classList.remove('hidden');
        return;
    }
    
    const dto = { name, permissionIds };
    
    try {
        if (id) {
            await API.updateRole(id, dto);
            alert('Cập nhật vai trò thành công.');
        } else {
            await API.createRole(dto);
            alert('Tạo vai trò thành công.');
        }
        closeModal('role-modal');
        loadRoles();
    } catch (err) {
        errorEl.textContent = err.message;
        errorEl.classList.remove('hidden');
    }
}

async function deleteRole(id) {
    if (confirm('Bạn có chắc chắn muốn xóa vai trò này? Cảnh báo: Việc xóa vai trò có thể ảnh hưởng đến người dùng đang có vai trò đó.')) {
        try {
            await API.deleteRole(id);
            alert('Đã xóa vai trò.');
            loadRoles();
        } catch (e) {
            alert('Lỗi: ' + e.message);
        }
    }
}
