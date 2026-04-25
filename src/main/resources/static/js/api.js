const API = {
    // Hàm gọi API kèm Token
    async fetchWithAuth(url, options = {}) {
        const token = localStorage.getItem('accessToken');
        const headers = { ...options.headers };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(url, { ...options, headers });

        if (response.status === 401 || response.status === 403) {
            // Token hết hạn hoặc không có quyền truy cập, chuyển hướng về trang login
            console.warn('[SECURITY] Token không hợp lệ hoặc đã hết hạn. Chuyển hướng đăng nhập...');
            localStorage.removeItem('accessToken');
            window.location.href = '/login.html';
            throw new Error('Unauthorized');
        }

        return response;
    },

    async login(username, password) {
        console.log('[DEBUG API] Gọi POST /api/auth/login');
        const res = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (!res.ok) {
            throw new Error('Đăng nhập thất bại');
        }
        
        const data = await res.json();
        localStorage.setItem('accessToken', data.accessToken);
        return data;
    },

    async getCurrentBatch() {
        console.log('[DEBUG API] Gọi GET /api/batch/current');
        const res = await this.fetchWithAuth(`${API_BASE_URL}/batch/current`);
        if (res.status === 204) {
            console.log('[DEBUG API] Server trả về 204 No Content (Không có batch hiện tại)');
            return null;
        }
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
    },

    async getAllBatches() {
        console.log('[DEBUG API] Gọi GET /api/batch/all');
        const res = await this.fetchWithAuth(`${API_BASE_URL}/batch/all`);
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
    },

    async getFruitsByBatch(batchId) {
        console.log(`[DEBUG API] Gọi GET /api/batch/${batchId}/fruits`);
        if (batchId === 'all') {
            const res = await this.fetchWithAuth(`${API_BASE_URL}/fruit/all`);
            if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
            return res.json();
        }
        const res = await this.fetchWithAuth(`${API_BASE_URL}/batch/${batchId}/fruits`);
        if (!res.ok) {
            console.error(`[DEBUG API] Lỗi HTTP ${res.status} khi lấy danh sách Fruits`);
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.json();
    },

    async getStats() {
        console.log('[DEBUG API] Gọi GET /api/fruit/stats');
        const res = await this.fetchWithAuth(`${API_BASE_URL}/fruit/stats`);
        if (!res.ok) {
            console.error(`[DEBUG API] Lỗi HTTP ${res.status} khi lấy stats`);
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.json();
    },

    async createNewBatch(name) {
        const formData = new FormData();
        if (name) formData.append('name', name);
        const res = await this.fetchWithAuth(`${API_BASE_URL}/batch/new`, {
            method: 'POST',
            body: formData
        });
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
    },

    async sendCommand(command) {
        const res = await this.fetchWithAuth(`${API_BASE_URL}/control`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(command)
        });
        return res.ok;
    },

    async exportBatch(batchId) {
        console.log(`[DEBUG API] Gọi GET /api/batch/${batchId}/export`);
        const res = await this.fetchWithAuth(`${API_BASE_URL}/batch/${batchId}/export`);
        if (!res.ok) {
            console.error(`[DEBUG API] Lỗi HTTP ${res.status} khi tải Excel`);
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.blob();
    }
};