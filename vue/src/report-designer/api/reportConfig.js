import fetch from '@/utils/fetch'

export function saveReportConfig(data) {
  return fetch({ url: '/api/report/config/save', method: 'post', data: data })
}

export function getReportConfigList() {
  return fetch({ url: '/api/report/config/list', method: 'get' })
}

export function getReportConfigDetail(reportCode) {
  return fetch({ url: '/api/report/config/' + reportCode, method: 'get' })
}

export function deleteReportConfig(reportCode) {
  return fetch({ url: '/api/report/config/' + reportCode, method: 'delete' })
}

export function executeReport(reportCode, pageParams) {
  return fetch({
    url: '/api/report/execute/' + reportCode,
    method: 'post',
    data: pageParams
  })
}

export function analyzeSql(sql, dbCode) {
  return fetch({
    url: '/api/report/analyze-sql',
    method: 'post',
    data: { sql: sql, dbCode: dbCode }
  })
}

export function exportReport(reportCode, params) {
  return fetch({
    url: '/api/report/export/' + reportCode,
    method: 'post',
    data: { params: params },
    responseType: 'arraybuffer'
  })
}
