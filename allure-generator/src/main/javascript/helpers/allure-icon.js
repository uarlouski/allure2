import {SafeString} from 'handlebars/runtime';
import translate from './t';

const icons = {
    flaky: {
        className: 'fa fa-bomb',
        tooltip: 'status.flaky'
    },
    newFailed: {
        className: 'fa fa-warning',
        tooltip: 'status.newFailed'
    },
    failed: {
        className: 'fa fa-times-circle fa-fw text_status_failed',
        tooltip: 'status.failed'
    },
    broken: {
        className: 'fa fa-exclamation-circle fa-fw text_status_broken',
        tooltip: 'status.broken'
    },
    passed: {
        className: 'fa fa-check-circle fa-fw text_status_passed',
        tooltip: 'status.passed'
    },
    knownissuesonly: {
        className: 'fa fa-check-circle fa-fw text_status_knownissuesonly',
        tooltip: 'status.knownissuesonly'
    },
    skipped: {
        className: 'fa fa-minus-circle fa-fw text_status_skipped',
        tooltip: 'status.skipped'
    },
    pending: {
        className: 'fa fa-question-circle fa-fw text_status_pending',
        tooltip: 'status.pending'
    }
};

export default function (value, extraClasses='') {
    const icon = icons[value];
    return icon ? new SafeString(`<span class="${icon.className} ${extraClasses}" data-tooltip="${translate(icon.tooltip)}"></span>`) : '';
}
