import fetch from '@/utils/fetch'

export function submitFormData(data) {
  return fetch({
    url: '/api/form/data/submit',
    method: 'post',
    data
  })
}

export function getFormDataList(formCode) {
  return fetch({
    url: `/api/form/data/list/${formCode}`,
    method: 'get'
  })
}

export function getFormDataDetail(id) {
  return fetch({
    url: `/api/form/data/${id}`,
    method: 'get'
  })
}

export function updateFormDataStatus(id, status) {
  return fetch({
    url: `/api/form/data/${id}/status`,
    method: 'post',
    params: { status }
  })
}
