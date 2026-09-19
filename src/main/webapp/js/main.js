/* ============================================================
   AYESHA MART - Main Application Script
   Contains: validation foundation, small UI helpers.
   Purpose: reusable JS foundation used by all pages.
   ============================================================ */

(function () {
    'use strict';

    /* ---------- Validation core ---------- */

    var RULES = {
        required: function (value) {
            return value.trim().length > 0;
        },
        email: function (value) {
            var re = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            return re.test(value.trim());
        },
        minLength: function (value, min) {
            return value.trim().length >= Number(min);
        },
        maxLength: function (value, max) {
            return value.trim().length <= Number(max);
        },
        numeric: function (value) {
            return /^[0-9]+(\.[0-9]{1,2})?$/.test(value.trim());
        },
        phone: function (value) {
            return /^[0-9+\-\s()]{7,15}$/.test(value.trim());
        },
        match: function (value, matchId) {
            var other = document.getElementById(matchId);
            return other ? value === other.value : true;
        },
        checked: function (value) {
            return value === 'on' || value === true;
        }
    };

    function parseRules(el) {
        var raw = el.getAttribute('data-validate');
        if (!raw) return [];
        return raw.split('|').filter(function (r) { return r.length > 0; });
    }

    function getErrorMessage(el, rule) {
        var shown = el.getAttribute('data-error-' + rule) || el.getAttribute('data-error');
        var defaults = {
            required: 'This field is required.',
            email: 'Please enter a valid email address.',
            minLength: 'This value is too short.',
            maxLength: 'This value is too long.',
            numeric: 'Please enter a valid number.',
            phone: 'Please enter a valid phone number.',
            match: 'The values do not match.',
            checked: 'Please accept this option.'
        };
        return shown || defaults[rule] || 'Invalid value.';
    }

    function isValid(el) {
        var rules = parseRules(el);
        for (var i = 0; i < rules.length; i++) {
            var rule = rules[i];
            var ruleName = rule;
            var ruleParam = null;

            var colon = rule.indexOf(':');
            if (colon > -1) {
                ruleName = rule.substring(0, colon);
                ruleParam = rule.substring(colon + 1);
            }

            var value = el.type === 'checkbox' || el.type === 'radio' ? el.checked : el.value;
            var pass;

            switch (ruleName) {
                case 'minLength':
                case 'maxLength':
                    pass = RULES[ruleName](value, ruleParam);
                    break;
                case 'match':
                    pass = RULES[ruleName](value, ruleParam);
                    break;
                default:
                    if (typeof RULES[ruleName] === 'function') {
                        pass = RULES[ruleName](value, ruleParam);
                    } else {
                        pass = true;
                    }
            }

            if (!pass) {
                showFieldError(el, getErrorMessage(el, ruleName));
                return false;
            }
        }
        clearFieldError(el);
        return true;
    }

    function showFieldError(el, message) {
        el.classList.add('is-invalid');
        el.classList.remove('is-valid');
        var fb = document.getElementById(el.id + '-feedback');
        if (fb) {
            fb.textContent = message;
            fb.style.display = 'block';
        }
    }

    function clearFieldError(el) {
        el.classList.remove('is-invalid');
        el.classList.add('is-valid');
        var fb = document.getElementById(el.id + '-feedback');
        if (fb) {
            fb.style.display = 'none';
        }
    }

    function clearFieldErrorSilently(el) {
        el.classList.remove('is-invalid', 'is-valid');
        var fb = document.getElementById(el.id + '-feedback');
        if (fb) {
            fb.style.display = 'none';
        }
    }

    function initValidation() {
        var forms = document.querySelectorAll('form[data-validate-form]');
        Array.prototype.forEach.call(forms, function (form) {
            var fields = form.querySelectorAll('[data-validate]');

            Array.prototype.forEach.call(fields, function (el) {
                el.addEventListener('blur', function () {
                    if (el.value || el.type === 'checkbox') {
                        isValid(el);
                    } else {
                        clearFieldErrorSilently(el);
                    }
                });
            });

            form.addEventListener('submit', function (event) {
                var allValid = true;
                Array.prototype.forEach.call(fields, function (el) {
                    if (!isValid(el)) allValid = false;
                });
                if (!allValid) {
                    event.preventDefault();
                    var firstInvalid = form.querySelector('.is-invalid');
                    if (firstInvalid) firstInvalid.focus();
                }
            });
        });
    }

    /* ---------- UI helpers ---------- */

    function autoDismissAlerts() {
        var alerts = document.querySelectorAll('.alert.alert-auto-dismiss');
        Array.prototype.forEach.call(alerts, function (alert) {
            setTimeout(function () {
                if (alert && alert.classList.contains('show')) {
                    var close = new bootstrap.Alert(alert);
                    close.close();
                }
            }, 6000);
        });
    }

    function formatPrice(value) {
        var num = Number(value);
        if (isNaN(num)) return value;
        return '\u20B9 ' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    /* ---------- Checkout payment method toggle ---------- */

    function syncPaymentBlock(block, visible, required) {
        if (!block) return;
        block.style.display = visible ? 'block' : 'none';
        Array.prototype.forEach.call(block.querySelectorAll('input, select, textarea'), function (inp) {
            if (inp.type !== 'radio' && inp.type !== 'checkbox') {
                inp.required = required;
            }
        });
    }

    function initPaymentOptions() {
        var upiBlock = document.getElementById('upiBlock');
        var cardBlock = document.getElementById('cardBlock');
        var radios = document.querySelectorAll('input[name="paymentMethod"]');
        if (!upiBlock && !cardBlock) return;
        if (!radios.length) return;

        var sync = function () {
            var checked = document.querySelector('input[name="paymentMethod"]:checked');
            var method = checked ? checked.value : 'COD';
            syncPaymentBlock(upiBlock, method === 'UPI', method === 'UPI');
            syncPaymentBlock(cardBlock, method === 'CARD', method === 'CARD');
        };

        Array.prototype.forEach.call(radios, function (radio) {
            radio.addEventListener('change', sync);
        });
        sync();
    }

    function initCardFormatting() {
        var card = document.querySelector('input[name="cardNumber"]');
        if (!card) return;
        card.addEventListener('input', function () {
            var digits = card.value.replace(/\D/g, '').substring(0, 16);
            var parts = digits.match(/.{1,4}/g) || [];
            card.value = parts.join(' ');
        });
    }

    /* ---------- Init ---------- */

    document.addEventListener('DOMContentLoaded', function () {
        initValidation();
        autoDismissAlerts();
        initPaymentOptions();
        initCardFormatting();
    });

    window.AyeshaMart = {
        RULES: RULES,
        isValid: isValid,
        formatPrice: formatPrice,
        showFieldError: showFieldError,
        clearFieldError: clearFieldError
    };
})();