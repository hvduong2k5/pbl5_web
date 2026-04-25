async function initAdminPage() {
    console.log('[DEBUG Admin] Bắt đầu initAdminPage...');
    
    // Kiểm tra quyền
    if (!hasAuthority('ROLE_ADMIN')) {
        alert('Access Denied: Bạn không có quyền truy cập trang này.');
        window.location.href = '/';
        return;
    }
    
    renderHeader(window.location.pathname, null);
    
    const userBody = document.getElementById('user-body');
    const roleBody = document.getElementById('role-body');
    
    let allUsers = [];
    let allRoles = [];
    let allKnownRoles = new Set(); // Sẽ được cập nhật từ API roles

    // Pagination states
    let userPage = 0;
    let userTotalPages = 1;
    let userKeyword = '';
    const size = 10;

    let rolePage = 0;
    let roleTotalPages = 1;

    // Khởi tạo DOM Elements trước khi gọi API
    const userModal = document.getElementById('user-modal');
    const roleModal = document.getElementById('role-modal');
    const userForm = document.getElementById('user-form');
    const roleForm = document.getElementById('role-form');

    // Pagination elements
    const btnUserPrev = document.getElementById('btn-user-prev');
    const btnUserNext = document.getElementById('btn-user-next');
    const btnRolePrev = document.getElementById('btn-role-prev');
    const btnRoleNext = document.getElementById('btn-role-next');

    // Search elements
    const userSearchInput = document.getElementById('user-search-input');
    const btnUserSearch = document.getElementById('btn-user-search');

    // Load data
    await loadPermissions();
    await loadRoles();
    await loadUsers();

    // --- Search Event Listeners ---
    if (btnUserSearch) {
        btnUserSearch.addEventListener('click', async () => {
            userKeyword = userSearchInput.value.trim();
            userPage = 0; // Reset về trang 1 khi search mới
            await loadUsers();
        });
    }

    if (userSearchInput) {
        userSearchInput.addEventListener('keypress', async (e) => {
            if (e.key === 'Enter') {
                userKeyword = userSearchInput.value.trim();
                userPage = 0;
                await loadUsers();
            }
        });
    }

    // --- Pagination Event Listeners ---
    if (btnUserPrev) {
        btnUserPrev.addEventListener('click', async () => {
            if (userPage > 0) {
                userPage--;
                await loadUsers();
            }
        });
    }
    if (btnUserNext) {
        btnUserNext.addEventListener('click', async () => {
            if (userPage < userTotalPages - 1) {
                userPage++;
                await loadUsers();
            }
        });
    }
    if (btnRolePrev) {
        btnRolePrev.addEventListener('click', async () => {
            if (rolePage > 0) {
                rolePage--;
                await loadRoles();
            }
        });
    }
    if (btnRoleNext) {
        btnRoleNext.addEventListener('click', async () => {
            if (rolePage < roleTotalPages - 1) {
                rolePage++;
                await loadRoles();
            }
        });
    }

    // Nút mở modal tạo user
    document.getElementById('btn-add-user').addEventListener('click', () => {
        document.getElementById('user-modal-title').textContent = 'Create New User';
        document.getElementById('userId').value = '';
        document.getElementById('username').value = '';
        document.getElementById('username').disabled = false;
        document.getElementById('password').value = '';
        document.getElementById('password').required = true;
        document.getElementById('enabled').checked = true;
        
        renderRoleCheckboxes([]);
        userModal.classList.remove('hidden');
    });

    // Nút mở modal tạo role
    document.getElementById('btn-add-role').addEventListener('click', () => {
        document.getElementById('role-modal-title').textContent = 'Create New Role';
        document.getElementById('roleId').value = '';
        document.getElementById('roleName').value = '';
        document.getElementById('roleName').disabled = false;
        
        // Bỏ check tất cả permission
        document.querySelectorAll('input[name="permissions"]').forEach(cb => cb.checked = false);
        document.getElementById('role-error').classList.add('hidden');
        roleModal.classList.remove('hidden');
    });

    // Submit form User
    userForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const errorDiv = document.getElementById('user-error');
        errorDiv.classList.add('hidden');

        const id = document.getElementById('userId').value;
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const enabled = document.getElementById('enabled').checked;

        const selectedRoles = Array.from(document.querySelectorAll('input[name="roles"]:checked')).map(cb => cb.value);

        if (selectedRoles.length === 0) {
            errorDiv.textContent = 'Phải chọn ít nhất 1 Role.';
            errorDiv.classList.remove('hidden');
            return;
        }

        try {
            if (id) {
                // Update
                const payload = { enabled, roles: selectedRoles };
                if (password) payload.password = password; // Chỉ gửi password nếu có thay đổi
                await API.updateUser(id, payload);
            } else {
                // Create
                const payload = { username, password, enabled, roles: selectedRoles };
                await API.createUser(payload);
            }
            userModal.classList.add('hidden');
            await loadUsers(); // reload bảng
        } catch (err) {
            errorDiv.textContent = err.message;
            errorDiv.classList.remove('hidden');
        }
    });

    // Submit form Role
    roleForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const errorDiv = document.getElementById('role-error');
        errorDiv.classList.add('hidden');

        const id = document.getElementById('roleId').value;
        const name = document.getElementById('roleName').value.trim();
        const permissionIds = Array.from(document.querySelectorAll('input[name="permissions"]:checked')).map(cb => parseInt(cb.value));

        if (permissionIds.length === 0) {
            errorDiv.textContent = 'Phải chọn ít nhất 1 Permission.';
            errorDiv.classList.remove('hidden');
            return;
        }

        try {
            if (id) {
                // Update
                await API.updateRole(id, { name, permissionIds });
                alert(`Cập nhật Role ${name.toUpperCase()} thành công!`);
            } else {
                // Create
                await API.createRole({ name, permissionIds });
                alert(`Tạo Role ${name.toUpperCase()} thành công!`);
            }
            roleModal.classList.add('hidden');
            await loadRoles(); // reload role table and role checkboxes
        } catch (err) {
            errorDiv.textContent = err.message;
            errorDiv.classList.remove('hidden');
        }
    });

    async function loadRoles() {
        try {
            const pageData = await API.getAllRoles(rolePage, size);
            allRoles = pageData.content || pageData; // Fallback in case backend returns list instead of Page
            roleTotalPages = pageData.totalPages || 1;
            
            if (document.getElementById('role-page-info')) {
                document.getElementById('role-page-info').textContent = `Page ${rolePage + 1} of ${roleTotalPages}`;
                btnRolePrev.disabled = rolePage === 0;
                btnRoleNext.disabled = rolePage >= roleTotalPages - 1;
            }

            allKnownRoles.clear();
            roleBody.innerHTML = '';
            
            allRoles.forEach(r => {
                allKnownRoles.add(r.name);
                
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${r.id}</td>
                    <td><strong>${r.name}</strong></td>
                    <td>${r.permissions ? r.permissions.map(p => `<span class="badge detected">${p.name}</span>`).join(' ') : 'Không có'}</td>
                    <td>
                        <button class="btn btn-secondary btn-edit-role" data-id="${r.id}" style="padding: 4px 8px; margin: 0 5px 0 0; display: inline-block; width: auto; font-size: 0.8em;">Edit</button>
                        <button class="btn btn-danger btn-delete-role" data-id="${r.id}" style="padding: 4px 8px; margin: 0; display: inline-block; width: auto; font-size: 0.8em;">Delete</button>
                    </td>
                `;
                roleBody.appendChild(tr);
            });
            
            // Gắn sự kiện edit/delete
            document.querySelectorAll('.btn-edit-role').forEach(btn => {
                btn.addEventListener('click', (e) => editRole(e.target.getAttribute('data-id')));
            });
            document.querySelectorAll('.btn-delete-role').forEach(btn => {
                btn.addEventListener('click', (e) => deleteRole(e.target.getAttribute('data-id')));
            });
            
        } catch (e) {
            console.error('[ERROR Admin] Lỗi khi lấy danh sách role:', e);
            roleBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: red;">Lỗi tải dữ liệu. Vui lòng kiểm tra Console.</td></tr>`;
        }
    }

    async function loadUsers() {
        try {
            const pageData = await API.getAllUsers(userKeyword, userPage, size);
            allUsers = pageData.content || pageData;
            userTotalPages = pageData.totalPages || 1;

            if (document.getElementById('user-page-info')) {
                document.getElementById('user-page-info').textContent = `Page ${userPage + 1} of ${userTotalPages}`;
                btnUserPrev.disabled = userPage === 0;
                btnUserNext.disabled = userPage >= userTotalPages - 1;
            }

            userBody.innerHTML = '';
            
            if (allUsers.length === 0) {
                userBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #666;">Không tìm thấy người dùng nào.</td></tr>`;
                return;
            }
            
            allUsers.forEach(u => {
                const tr = document.createElement('tr');
                const badgeClass = u.enabled ? 'badge sorted' : 'badge rotten';
                const statusText = u.enabled ? 'Active' : 'Disabled';
                
                tr.innerHTML = `
                    <td>${u.id}</td>
                    <td><strong>${u.username}</strong></td>
                    <td><span class="${badgeClass}">${statusText}</span></td>
                    <td>${u.roles.map(r => `<span class="badge classified">${r}</span>`).join(' ')}</td>
                    <td>${formatDate(u.createdAt)}</td>
                    <td>
                        <button class="btn btn-secondary btn-edit-user" data-id="${u.id}" style="padding: 4px 8px; margin: 0 5px 0 0; display: inline-block; width: auto; font-size: 0.8em;">Edit</button>
                        <button class="btn btn-danger btn-delete-user" data-id="${u.id}" style="padding: 4px 8px; margin: 0; display: inline-block; width: auto; font-size: 0.8em;">Delete</button>
                    </td>
                `;
                userBody.appendChild(tr);
            });

            // Gắn sự kiện cho các nút Edit và Delete
            document.querySelectorAll('.btn-edit-user').forEach(btn => {
                btn.addEventListener('click', (e) => editUser(e.target.getAttribute('data-id')));
            });

            document.querySelectorAll('.btn-delete-user').forEach(btn => {
                btn.addEventListener('click', (e) => deleteUser(e.target.getAttribute('data-id')));
            });

        } catch (e) {
            console.error('[ERROR Admin] Lỗi khi lấy danh sách user:', e);
            userBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red;">Lỗi tải dữ liệu. Vui lòng kiểm tra Console.</td></tr>`;
        }
    }

    async function loadPermissions() {
        try {
            const perms = await API.getAllPermissions();
            const container = document.getElementById('permission-checkboxes');
            container.innerHTML = '';
            perms.forEach(p => {
                container.innerHTML += `
                    <div class="checkbox-item">
                        <input type="checkbox" id="perm-${p.id}" name="permissions" value="${p.id}" />
                        <label for="perm-${p.id}">${p.name}</label>
                    </div>
                `;
            });
        } catch (e) {
            console.error('[ERROR Admin] Lỗi khi tải Permissions:', e);
        }
    }

    function renderRoleCheckboxes(selectedRoles) {
        const container = document.getElementById('role-checkboxes');
        container.innerHTML = '';
        allKnownRoles.forEach(r => {
            const checked = selectedRoles.includes(r) ? 'checked' : '';
            container.innerHTML += `
                <div class="checkbox-item">
                    <input type="checkbox" id="role-${r}" name="roles" value="${r}" ${checked} />
                    <label for="role-${r}">${r}</label>
                </div>
            `;
        });
    }

    function editUser(id) {
        const u = allUsers.find(user => user.id == id);
        if (!u) return;

        document.getElementById('user-modal-title').textContent = 'Edit User: ' + u.username;
        document.getElementById('userId').value = u.id;
        document.getElementById('username').value = u.username;
        document.getElementById('username').disabled = true; // Không cho sửa username
        document.getElementById('password').value = ''; // Để trống là không đổi pass
        document.getElementById('password').required = false; 
        document.getElementById('enabled').checked = u.enabled;
        
        renderRoleCheckboxes(u.roles);
        
        document.getElementById('user-error').classList.add('hidden');
        userModal.classList.remove('hidden');
    }

    async function deleteUser(id) {
        if (!confirm('Bạn có chắc chắn muốn xóa user ID = ' + id + ' không?')) return;
        try {
            await API.deleteUser(id);
            await loadUsers();
        } catch (e) {
            alert('Lỗi: ' + e.message);
        }
    }

    function editRole(id) {
        const r = allRoles.find(role => role.id == id);
        if (!r) return;

        document.getElementById('role-modal-title').textContent = 'Edit Role: ' + r.name;
        document.getElementById('roleId').value = r.id;
        document.getElementById('roleName').value = r.name;
        
        if (r.name === 'ADMIN' || r.name === 'OPERATOR') {
            document.getElementById('roleName').disabled = true;
        } else {
            document.getElementById('roleName').disabled = false;
        }
        
        // Reset permissions
        document.querySelectorAll('input[name="permissions"]').forEach(cb => cb.checked = false);
        // Check assigned permissions
        if (r.permissions) {
            r.permissions.forEach(p => {
                const cb = document.getElementById(`perm-${p.id}`);
                if (cb) cb.checked = true;
            });
        }
        
        document.getElementById('role-error').classList.add('hidden');
        roleModal.classList.remove('hidden');
    }

    async function deleteRole(id) {
        if (!confirm('Bạn có chắc chắn muốn xóa role ID = ' + id + ' không? Lưu ý: Xóa role sẽ ảnh hưởng tới các user đang mang role này.')) return;
        try {
            await API.deleteRole(id);
            await loadRoles();
        } catch (e) {
            alert('Lỗi: ' + e.message);
        }
    }
}

document.addEventListener('DOMContentLoaded', initAdminPage);
