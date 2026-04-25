import fetch from '@/utils/fetch'

/**
 * 分页查询
 */
export const queryTablePagedUrl = '/wms/custom_fields'
export const queryColumn = (tableName) => fetch.get(`/wms/custom_fields/${tableName}`)
export const addTable = (tableName) => fetch.post(`/wms/custom_fields/${tableName}`)
export const delTable = (tableName) => fetch.delete(`/wms/custom_fields/${tableName}`)
export const saveColumns = (tableName, dto) => fetch.post(`/wms/custom_fields/${tableName}/`, dto)
