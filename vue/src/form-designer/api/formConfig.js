import fetch from '@/utils/fetch'

export function saveFormConfig(data) {
  return fetch({
    url: '/api/form/config/save',
    method: 'post',
    data
  })
}

export function getFormConfigList() {
  return fetch({
    url: '/api/form/config/list',
    method: 'get'
  })
}

export function getFormConfigDetail(code) {
  return fetch({
    url: `/api/form/config/${code}`,
    method: 'get'
  })
}

export function validateFormConfig(configJson) {
  return fetch({
    url: '/api/form/config/validate',
    method: 'post',
    data: configJson
  })
}
