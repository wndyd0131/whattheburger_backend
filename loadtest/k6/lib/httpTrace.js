/** Client trace id + deduct POST failure logging (loadtest). */

export const TRACE_HEADER = 'X-Trace-Id';

const BODY_LOG_MAX = 2048;

export function newDeductTraceId() {
  return `k6-${__VU}-${__ITER}-${Date.now()}`;
}

export function deductRequestHeaders(traceId) {
  return {
    'Content-Type': 'application/json',
    [TRACE_HEADER]: traceId,
  };
}

export function getResponseHeader(res, name) {
  if (!res?.headers || name == null) {
    return null;
  }
  const target = String(name).toLowerCase();
  for (const key of Object.keys(res.headers)) {
    if (String(key).toLowerCase() === target) {
      const val = res.headers[key];
      return val == null ? null : String(val);
    }
  }
  return null;
}

function truncateBody(text) {
  if (text == null) {
    return '';
  }
  const s = String(text);
  if (s.length <= BODY_LOG_MAX) {
    return s;
  }
  return `${s.slice(0, BODY_LOG_MAX)}...(truncated)`;
}

export function logDeductFailure({ traceId, storeId, url, res, body }) {
  const status = res?.status;
  const responseTraceId = getResponseHeader(res, TRACE_HEADER);
  const contentType = getResponseHeader(res, 'Content-Type');
  const bodyText =
    res?.body != null && String(res.body).length > 0
      ? truncateBody(res.body)
      : body != null
        ? truncateBody(JSON.stringify(body))
        : '';

  const lines = [
    '[deduct] request failed',
    `  traceId=${traceId}`,
    responseTraceId != null ? `  responseTraceId=${responseTraceId}` : null,
    `  storeId=${storeId}`,
    `  url=${url}`,
    `  status=${status}`,
    contentType != null ? `  contentType=${contentType}` : null,
    bodyText ? `  body=${bodyText}` : '  body=(empty)',
  ].filter(Boolean);

  console.error(lines.join('\n'));
}
