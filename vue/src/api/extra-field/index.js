import fetch from '@/utils/fetch'
import qs from 'qs'

export const extraFieldPagedUrl = '/wms/customize'
export const getExtraFieldList = (params) => fetch.get('/wms/customize', {
  params
})
export const editExtraField = (params) => fetch.post('/wms/customize/create', params)
export const delExtraField = (params) => fetch.post(`/wms/customize/del`, qs.stringify(params))
