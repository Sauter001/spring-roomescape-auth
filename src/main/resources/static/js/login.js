(() => {
    const form = document.getElementById('login-form');
    if (!form) return;

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const uid = form.elements['uid'].value.trim();
        const password = form.elements['password'].value;

        if (!uid || !password) {
            showToast('아이디와 비밀번호를 모두 입력하세요.', 'error');
            return;
        }

        try {
            await apiFetch('/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({uid, password})
            });
            window.location.href = '/home';
        } catch (error) {
            toastError(error);
        }
    });
})();
