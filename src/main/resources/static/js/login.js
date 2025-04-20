document.addEventListener('DOMContentLoaded', function() {
    // 添加输入框焦点效果
    const inputs = document.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
        });
        input.addEventListener('blur', function() {
            if (!this.value) {
                this.parentElement.classList.remove('focused');
            }
        });
    });

    // 表单提交前验证
    const form = document.querySelector('form');
    form.addEventListener('submit', function(e) {
        const email = form.querySelector('input[type="email"]').value;
        const password = form.querySelector('input[type="password"]').value;

        if (!email || !password) {
            e.preventDefault();
            showError('请填写所有必填字段');
        }
    });
});

function showError(message) {
    const errorDiv = document.querySelector('.error');
    if (errorDiv) {
        errorDiv.textContent = message;
    } else {
        const div = document.createElement('div');
        div.className = 'error';
        div.textContent = message;
        document.querySelector('.login-container').insertBefore(div, document.querySelector('form'));
    }
} 