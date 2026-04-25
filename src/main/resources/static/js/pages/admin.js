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
    let allUsers = [];
    const allKnownRoles = new Set(['ADMIN', 'OPERATOR']);

    // Load data
    await loadPermissions();
    await loadUsers();

    // DOM Elements
    const userModal = document.getElementById('user-modal');
    const roleModal = document.getElementById('role-modal');
    const userForm = document.getElementById('user-form');
    const roleForm = document.getElementById('role-form');

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
        document.getElementById('roleName').value = '';
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
            loadUsers(); // reload bảng
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

        const name = document.getElementById('roleName').value.trim();
        const permissionIds = Array.from(document.querySelectorAll('input[name="permissions"]:checked')).map(cb => parseInt(cb.value));

        if (permissionIds.length === 0) {
            errorDiv.textContent = 'Phải chọn ít nhất 1 Permission.';
            errorDiv.classList.remove('hidden');
            return;
        }

        try {
            await API.createRole({ name, permissionIds });
            allKnownRoles.add(name.toUpperCase());
            roleModal.classList.add('hidden');
            alert(`Tạo Role ${name.toUpperCase()} thành công!`);
        } catch (err) {
            errorDiv.textContent = err.message;
            errorDiv.classList.remove('hidden');
        }
    });

    async function loadUsers() {
        try {
            allUsers = await API.getAllUsers();
            userBody.innerHTML = '';
            
            allUsers.forEach(u => {
                u.roles.forEach(r => allKnownRoles.add(r)); // Thu thập các role động

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
                        <button class="btn btn-secondary btn-edit" data-id="${u.id}" style="padding: 4px 8px; margin: 0 5px 0 0; display: inline-block; width: auto; font-size: 0.8em;">Edit</button>
                        <button class="btn btn-danger btn-delete" data-id="${u.id}" style="padding: 4px 8px; margin: 0; display: inline-block; width: auto; font-size: 0.8em;">Delete</button>
                    </td>
                `;
                userBody.appendChild(tr);
            });

            // Gắn sự kiện cho các nút Edit và Delete
            document.querySelectorAll('.btn-edit').forEach(btn => {
                btn.addEventListener('click', (e) => editUser(e.target.getAttribute('data-id')));
            });

            document.querySelectorAll('.btn-delete').forEach(btn => {
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
            loadUsers();
        } catch (e) {
            alert('Lỗi: ' + e.message);
        }
    }
}

document.addEventListener('DOMContentLoaded', initAdminPage);