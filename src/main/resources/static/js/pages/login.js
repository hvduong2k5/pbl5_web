document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const btnSubmit = document.getElementById('btn-submit');
    const btnText = document.getElementById('btn-text');
    const errorMessage = document.getElementById('error-message');

    // Kiểm tra nếu đã login thì redirect về home
    if (localStorage.getItem('accessToken')) {
        window.location.href = '/';
        return;
    }

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();

        if (!username || !password) {
            showError('Vui lòng nhập đầy đủ tài khoản và mật khẩu.');
            return;
        }

        // Bật trạng thái loading
        setLoading(true);

        try {
            await API.login(username, password);
            // Đăng nhập thành công -> Chuyển hướng
            window.location.href = '/';
        } catch (error) {
            console.error('[ERROR Login] Đăng nhập thất bại:', error);
            showError('Tên đăng nhập hoặc mật khẩu không chính xác.');
            // Tắt trạng thái loading để thử lại
            setLoading(false);
        }
    });

    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.classList.remove('hidden');
        
        // Auto-hide sau 5 giây
        setTimeout(() => {
            errorMessage.classList.add('hidden');
        }, 5000);
    }

    function setLoading(isLoading) {
        if (isLoading) {
            btnSubmit.disabled = true;
            btnText.textContent = 'Đang đăng nhập...';
            errorMessage.classList.add('hidden');
        } else {
            btnSubmit.disabled = false;
            btnText.textContent = 'Sign In';
        }
    }
});