import fetch from '@/utils/fetch'

/**
 * 入库单主表分页查询
 * @param {Object} params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export const getInboundMasterList = (params) => fetch.get('/platform/definition/inbound/master', { params })

/**
 * 入库单明细分页查询
 * @param {Object} params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export const getInboundDetailList = (params) => fetch.get('/platform/definition/inbound/detail', { params })

/**
 * 提交过账入库单
 * @param {Object} data 提交数据，包含ids数组
 * @returns {Promise<AxiosResponse<any>>}
 */
export const submitInbound = (data) => fetch.post('/platform/definition/inbound/submit', data)

/**
 * 作废入库单
 * @param {Object} data 作废数据，包含ids数组
 * @returns {Promise<AxiosResponse<any>>}
 */
export const cancelInbound = (data) => fetch.post('/platform/definition/inbound/cancel', data)

/**
 * 创建入库单
 * @param {Object} data 创建数据
 * @returns {Promise<AxiosResponse<any>>}
 */
export const createInbound = (data) => fetch.post('/platform/definition/inbound/create', data)

/**
 * 获取入库单状态枚举
 * @returns {Promise<Array>}
 */
export async function getInboundStatusEnum() {
  const result = await fetch.get('/platform/definition/enum/inbound/status')
  return result.map(item => ({
    label: item.label,
    value: item.value
  }))
}

/**
 * 获取入库单类型枚举
 * @returns {Promise<Array>}
 */
export async function getInboundTypeEnum() {
  const result = await fetch.get('/platform/definition/enum/inbound/type')
  return result.map(item => ({
    label: item.label,
    value: item.value
  }))
}

/**
 * 根据主单ID获取明细列表（备用接口）
 * @param {number} masterId 主单ID
 * @param {Object} params 其他查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export const getDetailByMasterId = (masterId, params = {}) =>
  fetch.get(`/platform/definition/inbound/${masterId}/details`, { params })

/**
 * 导出入库单
 * @param {Object} params 查询参数
 * @returns {Promise<AxiosResponse<any>>}
 */
export const exportInboundList = (params) => fetch.get('/platform/definition/inbound/export', {
  params,
  responseType: 'blob'
})

/**
 * 批量删除入库单
 * @param {Array} ids 入库单ID数组
 * @returns {Promise<AxiosResponse<any>>}
 */
export const deleteInboundList = (ids) => fetch.post('/platform/definition/inbound/delete', { ids })

/**
 * 获取仓库列表（用于下拉选择）
 * @returns {Promise<Array>}
 */
export async function getWarehouseList() {
  const result = await fetch.get('/warehouse/definition/warehouse/list')
  return result.map(item => ({
    label: item.warehouse_name,
    value: item.warehouse_code
  }))
}

/**
 * 获取供应商列表（用于下拉选择）
 * @returns {Promise<Array>}
 */
export async function getSupplierList() {
  const result = await fetch.get('/platform/definition/supplier/list')
  return result.map(item => ({
    label: item.supplier_name,
    value: item.supplier_code
  }))
}