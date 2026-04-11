const API = {
    async getCurrentBatch() {
        console.log('[DEBUG API] Gọi GET /api/batch/current');
        const res = await fetch(`${API_BASE_URL}/batch/current`);
        if (res.status === 204) {
            console.log('[DEBUG API] Server trả về 204 No Content (Không có batch hiện tại)');
            return null;
        }
        if (!res.ok) {
            console.error(`[DEBUG API] Lỗi HTTP ${res.status} khi lấy current batch`);
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.json();
    },
    async getAllBatches() {
        console.log('[DEBUG API] Gọi GET /api/batch/all');
        const res = await fetch(`${API_BASE_URL}/batch/all`);
        return res.json();
    },
    async getFruitsByBatch(batchId) {
        console.log(`[DEBUG API] Gọi GET /api/batch/${batchId}/fruits`);
        if (batchId === 'all') {
            const res = await fetch(`${API_BASE_URL}/fruit/all`);
            return res.json();
        }
        const res = await fetch(`${API_BASE_URL}/batch/${batchId}/fruits`);
        if (!res.ok) {
            console.error(`[DEBUG API] Lỗi HTTP ${res.status} khi lấy danh sách Fruits`);
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.json();
    },
    async getStats() {
        console.log('[DEBUG API] Gọi GET /api/fruit/stats');
        const res = await fetch(`${API_BASE_URL}/fruit/stats`);
        if (!res.ok) {
            console.error(`[DEBUG API] Lỗi HTTP ${res.status} khi lấy stats`);
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.json();
    },
    async createNewBatch(name) {
        const formData = new FormData();
        if (name) formData.append('name', name);
        const res = await fetch(`${API_BASE_URL}/batch/new`, {
            method: 'POST',
            body: formData
        });
        return res.json();
    },
    async sendCommand(command) {
        const res = await fetch(`${API_BASE_URL}/control`, {
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
        const res = await fetch(`${API_BASE_URL}/batch/${batchId}/export`);
        if (!res.ok) {
            console.error(`[DEBUG API] Lỗi HTTP ${res.status} khi tải Excel`);
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.blob();
    }
};