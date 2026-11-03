/**
 * FASHION SHOP - CART MANAGEMENT JS (AJAX Real-time)
 */

function changeQty(btn, delta) {
    const stepper = btn.closest('.quantity-stepper');
    const input = stepper.querySelector('.qty-input');
    let currentVal = parseInt(input.value) || 1;
    let maxVal = parseInt(input.getAttribute('max')) || 999;
    let newVal = currentVal + delta;

    if (newVal < 1) newVal = 1;
    if (newVal > maxVal) newVal = maxVal;

    if (newVal !== currentVal) {
        input.value = newVal;
        handleQtyUpdate(input);
    }
}

function onQtyInputChange(input) {
    let currentVal = parseInt(input.value) || 1;
    let maxVal = parseInt(input.getAttribute('max')) || 999;

    if (currentVal < 1) currentVal = 1;
    if (currentVal > maxVal) currentVal = maxVal;

    input.value = currentVal;
    handleQtyUpdate(input);
}

function handleQtyUpdate(input) {
    const productId = input.getAttribute('data-product-id');
    const unitPrice = parseFloat(input.getAttribute('data-unit-price')) || 0;
    const qty = parseInt(input.value) || 1;

    // Cập nhật ngay tạm tính của dòng sản phẩm
    const itemSubtotal = qty * unitPrice;
    const subtotalEl = document.getElementById('subtotal-' + productId);
    if (subtotalEl) {
        subtotalEl.innerText = formatCurrencyVND(itemSubtotal);
    }

    // Cập nhật ngay tổng cộng toàn giỏ hàng
    let grandTotal = 0;
    document.querySelectorAll('.qty-input').forEach(inp => {
        const pPrice = parseFloat(inp.getAttribute('data-unit-price')) || 0;
        const pQty = parseInt(inp.value) || 1;
        grandTotal += (pPrice * pQty);
    });

    const summarySubtotal = document.getElementById('summary-subtotal');
    const summaryGrandTotal = document.getElementById('summary-grand-total');
    if (summarySubtotal) summarySubtotal.innerText = formatCurrencyVND(grandTotal);
    if (summaryGrandTotal) summaryGrandTotal.innerText = formatCurrencyVND(grandTotal);

    // Gửi AJAX ngầm cập nhật database (debounce 250ms)
    clearTimeout(input.syncTimer);
    input.syncTimer = setTimeout(() => {
        fetch('/cart/api/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
                productId: productId,
                quantity: qty
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                if (data.subTotal !== undefined && subtotalEl) {
                    subtotalEl.innerText = formatCurrencyVND(data.subTotal);
                }
                if (data.total !== undefined) {
                    if (summarySubtotal) summarySubtotal.innerText = formatCurrencyVND(data.total);
                    if (summaryGrandTotal) summaryGrandTotal.innerText = formatCurrencyVND(data.total);
                }
            }
        })
        .catch(err => {
            console.error('Lỗi cập nhật giỏ hàng:', err);
        });
    }, 250);
}
