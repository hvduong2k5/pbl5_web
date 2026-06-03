async function initHistoryPage() {
    console.log('[DEBUG History] Bắt đầu initHistoryPage...');

    // Init sidebar
    initSidebar();

    const batchSelect = document.getElementById('batch-select');
    const historyBody = document.getElementById('history-body');
    const btnExport = document.getElementById('btn-export');

    try {
        const batches = await API.getAllBatches();
        batches.forEach(b => {
            const opt = document.createElement('option');
            opt.value = b.id;
            opt.textContent = b.name;
            batchSelect.appendChild(opt);
        });
    } catch (e) {
        console.error('[ERROR History] Lỗi khi load danh sách lô:', e);
    }

    async function loadHistory() {
        try {
            const val = batchSelect.value;
            const fruits = await API.getFruitsByBatch(val);
            historyBody.innerHTML = '';
            
            const mobileContainer = document.getElementById('mobile-history-cards');
            if (mobileContainer) mobileContainer.innerHTML = '';

            if (fruits.length === 0) {
                historyBody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align:center; padding: 40px; color: var(--text-muted);">
                            Không có dữ liệu
                        </td>
                    </tr>`;
                if (mobileContainer) mobileContainer.innerHTML = `<div style="text-align:center; padding: 40px; color: var(--text-muted);">Không có dữ liệu</div>`;
                return;
            }

            fruits.forEach(f => {
                const tr = document.createElement('tr');

                const imgHtml = f.imageUrl
                    ? `<img src="${f.imageUrl}" alt="${f.id}" class="table-img">`
                    : `<div class="table-no-img">N/A</div>`;

                const labelVi = getLabelVietnamese(f.label);
                const badgeClass = getBadgeClass(f.label);

                const confidenceHtml = f.confidence !== undefined && f.confidence !== null
                    ? `${(f.confidence * 100).toFixed(1)}%`
                    : 'N/A';

                const fmtCreated = f.createdAt ? formatDate(f.createdAt).replace('\n', '<br>') : '';
                const fmtClassified = f.classifiedAt ? formatDate(f.classifiedAt).replace('\n', '<br>') : '';
                const fmtSorted = f.sortedAt ? formatDate(f.sortedAt).replace('\n', '<br>') : '';

                tr.innerHTML = `
                    <td style="font-weight:600;">#${f.id}</td>
                    <td>${imgHtml}</td>
                    <td>${f.label ? `<span class="badge ${badgeClass}">${labelVi}</span>` : ''}</td>
                    <td style="font-size:0.85em; color:var(--text-secondary);">${fmtCreated}</td>
                    <td style="font-size:0.85em; color:var(--text-secondary);">${fmtClassified}</td>
                    <td style="font-size:0.85em; color:var(--text-secondary);">${fmtSorted}</td>
                    <td style="font-weight:600;">${confidenceHtml}</td>
                `;
                historyBody.appendChild(tr);

                if (mobileContainer) {
                    const cardHtml = `
                        <div class="mobile-list-card">
                            <div class="card-img-col">
                                ${f.imageUrl ? `<img src="${f.imageUrl}" alt="${f.id}">` : 'N/A'}
                            </div>
                            <div class="card-content-col">
                                <div class="card-title-row">
                                    <span class="card-id">ID: ${f.id}</span>
                                    <span class="card-time">${f.createdAt ? formatDate(f.createdAt) : ''}</span>
                                </div>
                                <div class="card-details-row">
                                    ${f.label ? `<span class="badge ${badgeClass}">${labelVi}</span>` : ''}
                                    <span class="card-detail-text">Độ tin cậy: ${confidenceHtml}</span>
                                </div>
                            </div>
                        </div>
                    `;
                    mobileContainer.insertAdjacentHTML('beforeend', cardHtml);
                }
            });
        } catch (e) {
            console.error('[ERROR History] Lỗi khi render bảng History:', e);
        }
    }

    // Export button
    if (btnExport) {
        if (!hasAuthority('EXPORT_DATA') && !hasAuthority('ROLE_ADMIN')) {
            btnExport.style.display = 'none';
        } else {
            btnExport.addEventListener('click', async () => {
                const val = batchSelect.value;
                if (val === 'all') {
                    alert('Vui lòng chọn một lô cụ thể để xuất dữ liệu.');
                    return;
                }

                const originalText = btnExport.textContent;
                btnExport.textContent = 'Đang xử lý...';
                btnExport.disabled = true;

                try {
                    const blob = await API.exportBatch(val);
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = `Lo_${val}_BaoCao.xlsx`;
                    document.body.appendChild(a);
                    a.click();
                    window.URL.revokeObjectURL(url);
                    a.remove();
                } catch (e) {
                    console.error('[ERROR History] Lỗi khi tải Excel:', e);
                    alert('Có lỗi xảy ra khi xuất file Excel.');
                } finally {
                    btnExport.textContent = originalText;
                    btnExport.disabled = false;
                }
            });
        }
    }

    batchSelect.addEventListener('change', loadHistory);
    loadHistory();
}

document.addEventListener('DOMContentLoaded', initHistoryPage);

