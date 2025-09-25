/**
 * Todo App - Client-side JavaScript
 * 사용자 경험 개선을 위한 인터랙티브 기능들
 */

document.addEventListener('DOMContentLoaded', function() {
    // 페이지 로드 시 애니메이션 적용
    initializeAnimations();

    // 폼 validation 강화
    enhanceFormValidation();

    // Todo 완료 토글 Ajax 처리
    initializeTodoToggle();

    // 알림 메시지 자동 닫기
    initializeAlertAutoClose();

    // 툴팁 초기화
    initializeTooltips();

    // 확인 대화상자 개선
    enhanceConfirmDialogs();

    console.log('Todo App initialized successfully!');
});

/**
 * 페이지 로드 애니메이션 초기화
 */
function initializeAnimations() {
    // 카드들에 fade-in 애니메이션 적용
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';

        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // 버튼들에 slide-in 애니메이션 적용
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
        });

        button.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
}

/**
 * 폼 validation 강화
 */
function enhanceFormValidation() {
    const forms = document.querySelectorAll('form');

    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // 커스텀 validation 로직
            const inputs = this.querySelectorAll('input[required], select[required], textarea[required]');
            let isValid = true;

            inputs.forEach(input => {
                if (!input.value.trim()) {
                    isValid = false;
                    showFieldError(input, '이 필드는 필수입니다.');
                } else {
                    clearFieldError(input);
                }
            });

            // 사용자명 길이 체크
            const usernameInput = this.querySelector('input[name="username"]');
            if (usernameInput && usernameInput.value.length < 4) {
                isValid = false;
                showFieldError(usernameInput, '사용자명은 4자 이상이어야 합니다.');
            }

            // 비밀번호 길이 체크
            const passwordInput = this.querySelector('input[name="password"]');
            if (passwordInput && passwordInput.value.length < 4) {
                isValid = false;
                showFieldError(passwordInput, '비밀번호는 4자 이상이어야 합니다.');
            }

            if (!isValid) {
                e.preventDefault();
                showAlert('입력 정보를 확인해주세요.', 'danger');
            } else {
                // 로딩 상태 표시
                const submitBtn = this.querySelector('button[type="submit"]');
                if (submitBtn) {
                    const originalText = submitBtn.innerHTML;
                    submitBtn.innerHTML = '<span class="loading-spinner"></span> 처리 중...';
                    submitBtn.disabled = true;

                    // 5초 후 원래 상태로 복구 (실패 시 대비)
                    setTimeout(() => {
                        submitBtn.innerHTML = originalText;
                        submitBtn.disabled = false;
                    }, 5000);
                }
            }
        });
    });
}

/**
 * Todo 완료 토글 Ajax 처리
 */
function initializeTodoToggle() {
    const toggleForms = document.querySelectorAll('form[action*="/toggle"]');

    toggleForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();

            const button = this.querySelector('button');
            const icon = button.querySelector('i');
            const originalClass = icon.className;

            // 로딩 상태 표시
            icon.className = 'fas fa-spinner fa-spin';
            button.disabled = true;

            // Ajax 요청 시뮬레이션 (실제로는 폼 제출)
            setTimeout(() => {
                // 실제 폼 제출
                this.submit();
            }, 500);
        });
    });
}

/**
 * 알림 메시지 자동 닫기
 */
function initializeAlertAutoClose() {
    const alerts = document.querySelectorAll('.alert');

    alerts.forEach(alert => {
        // 5초 후 자동으로 fade out
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';

            setTimeout(() => {
                if (alert.parentNode) {
                    alert.parentNode.removeChild(alert);
                }
            }, 500);
        }, 5000);
    });
}

/**
 * Bootstrap 툴팁 초기화
 */
function initializeTooltips() {
    // Bootstrap 5의 툴팁 초기화
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

/**
 * 확인 대화상자 개선
 */
function enhanceConfirmDialogs() {
    const deleteForms = document.querySelectorAll('form[action*="/delete"]');

    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();

            // 커스텀 확인 대화상자
            if (showCustomConfirm('정말 삭제하시겠습니까?', '이 작업은 되돌릴 수 없습니다.')) {
                this.submit();
            }
        });
    });
}

/**
 * 필드 에러 표시
 */
function showFieldError(input, message) {
    clearFieldError(input);

    input.classList.add('is-invalid');

    const errorDiv = document.createElement('div');
    errorDiv.className = 'invalid-feedback';
    errorDiv.textContent = message;

    input.parentNode.appendChild(errorDiv);
}

/**
 * 필드 에러 제거
 */
function clearFieldError(input) {
    input.classList.remove('is-invalid');

    const errorDiv = input.parentNode.querySelector('.invalid-feedback');
    if (errorDiv) {
        errorDiv.parentNode.removeChild(errorDiv);
    }
}

/**
 * 알림 메시지 표시
 */
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        <i class="fas fa-${getAlertIcon(type)}"></i> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    // 기존 알림 메시지들 위에 새 알림 추가
    const container = document.querySelector('.container');
    const firstChild = container.firstElementChild;
    container.insertBefore(alertDiv, firstChild);

    // 자동 닫기
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.style.opacity = '0';
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.parentNode.removeChild(alertDiv);
                }
            }, 300);
        }
    }, 4000);
}

/**
 * 알림 타입별 아이콘 반환
 */
function getAlertIcon(type) {
    const icons = {
        'success': 'check-circle',
        'danger': 'exclamation-circle',
        'warning': 'exclamation-triangle',
        'info': 'info-circle'
    };
    return icons[type] || 'info-circle';
}

/**
 * 커스텀 확인 대화상자
 */
function showCustomConfirm(title, message) {
    // 간단한 confirm 대화상자 (실제 프로젝트에서는 모달로 개선 가능)
    return confirm(`${title}\n\n${message}`);
}

/**
 * 진행률 바 애니메이션
 */
function animateProgressBar(progressBar, targetWidth) {
    let currentWidth = 0;
    const increment = targetWidth / 50;

    const animate = () => {
        if (currentWidth < targetWidth) {
            currentWidth += increment;
            progressBar.style.width = currentWidth + '%';
            requestAnimationFrame(animate);
        } else {
            progressBar.style.width = targetWidth + '%';
        }
    };

    animate();
}

/**
 * 페이지별 특수 기능 초기화
 */
function initializePageSpecificFeatures() {
    const currentPath = window.location.pathname;

    if (currentPath.includes('/dashboard')) {
        // 대시보드 페이지의 진행률 바 애니메이션
        const progressBars = document.querySelectorAll('.progress-bar');
        progressBars.forEach(bar => {
            const targetWidth = parseFloat(bar.style.width);
            if (targetWidth > 0) {
                bar.style.width = '0%';
                setTimeout(() => {
                    animateProgressBar(bar, targetWidth);
                }, 500);
            }
        });
    }

    if (currentPath.includes('/todos')) {
        // Todo 목록 페이지의 필터 버튼 활성화 표시
        const filterButtons = document.querySelectorAll('.btn-group .btn');
        filterButtons.forEach(button => {
            if (button.href === window.location.href) {
                button.classList.add('active');
            }
        });
    }
}

// 페이지별 특수 기능 초기화를 DOM 로드 후 실행
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(initializePageSpecificFeatures, 100);
});
