import fetch from '@/utils/fetch'

/**
 * 出库单分页查询api
 * @type {string}
 */
// export const queryOutBoundPagedUrl = '/platform/definition/outbound'
export const queryOutBoundPagedUrl = '/wms/requisition/outBoundRequisition'

// export const addOutbound = (dto) => fetch.post('/platform/definition/create', dto)
// export const addOutbound = (dto) => fetch.post('/wms/requisition/outBoundRequisition/outBound', dto)

/**
 * 出库审核
 * @param ids
 * @returns {Promise<AxiosResponse<any>>}
 */
export const finishOutboundList = (ids) => fetch.post('/platform/definition/outbound/audit', ids)

/**
 * 出库单作废
 * @param ids
 * @returns {Promise<AxiosResponse<any>>}
 */
export const cancelOutboundList = (ids) => fetch.post('/platform/definition/outbound/cancel', ids)
/**
 * 取出托盘
 * @param param
 * @returns {Promise<AxiosResponse<any>>}
 */
export const takeOutOutbound = (param) => fetch.post('/platform/definition/outbound/taskOut', param)
export const takeOutOutboundItem = (ids) => fetch.post('/wms/outbound_items/put', ids)

/**
 * 取出托盘-安排托盘明细页面
 * @param ids
 * @returns {Promise<AxiosResponse<any>>}
 */
export const takeOutOutboundAllocation = (param) => fetch.post('/platform/definition/requisitionDetail/taskOut', param)

/**
 * 出库单分配托盘
 * @param ids 出库单ID集合
 * @returns {Promise<AxiosResponse<any>>}
 */
export const allocateOutboundList = (id) => fetch.put('/platform/definition/requisitionOrder/allotOrder', { id })
export const deleteList = (ids) => fetch.post('/wms/outbounds/delete_list', ids)

/**
 * 根据出库单号查询出库明细
 * @param formNo
 * @returns {string}
 */
export const querySkuListByFormNo = (formNo) => `/platform/definition/outboundItem/${formNo}`
export const manualAllocate = (dto) => fetch.post('wms/outbound_items/manual_allocate', dto)
export const autoAllocate = (dto) => fetch.post('wms/outbound_items/auto_allocate', dto)
export const deleteItemList = (ids) => fetch.delete('/wms/outbound_items', ids)

/**
 * 根据出库明细单号获取安排托盘信息
 * @param formNo 出库明细单号
 * @returns {string}
 */
export const queryAllocationByItemFormNo = (formNo) => `/platform/definition/outboundAllocationPallet/${formNo}`

/**
 * 出库分配托盘明细作废
 * @param ids
 * @returns {Promise<AxiosResponse<any>>}
 */
export const deleteAllocationList = (ids) => fetch.post('/platform/definition/outboundAllocationPallet/cancel', ids)
export const finishAllocationList = (ids) => fetch.post('/wms/outbound_allocations/taskStatus/finish', ids)
export const finishPutList = (id) => fetch.patch('/wms/outbound_allocations', id)
export const cancelfinishList = (ids) => fetch.post('/wms/outbound_allocations/taskStatus/cancel', ids)

export const outboundAmount = (dto) => fetch.get('/wms/custom/outbound/amount', dto)

/**
 * 根据出库安排单据号获取托盘内物料明细
 * @param formNo
 * @returns {string}
 */
export const queryAllocationByPalletNo = (formNo, formId, itemId = '') => `/platform/definition/requisitionDetail?form_no=${formNo}&form_id=${formId}&item_id=${itemId}`

export const getAllocation = (formNo, formId) => fetch.get(queryAllocationByPalletNo(formNo, formId))

/**
 * 获取仓库号
 * @returns {string}
 */
export async function queryAllHouse() {
  const result = await fetch.get('/warehouse/definition/warehouse/houseNos')
  return result.map((item, index) => ({
    label: item['house_name'],
    value: item['house_no']
  }))
}

/**
 * 选择出库口
 * @returns {Promise<{label: *, value: string}[]>}
 */
export async function getMaterialOutLocations(ids) {
  const {rows: result} = await fetch.get('/platform/definition/outbound/out/location', {
    params: {
      ids: ids.join(',')
    }
  })
  return result.map(item => ({
    label: item['device_name'],
    value: item['device_code']
  }))
}

/**
 * 根据出库单安排托盘明细获取可用出库库位
 * @param ids
 * @returns {Promise<*>}
 */
export async function getLocations(ids) {
  const {rows: result} = await fetch.get('/platform/definition/outboundAllocationPallet/out/location', {
    params: {
      ids: ids.join(',')
    }
  })
  return result.map(item => ({
    label: item['device_name'],
    value: item['device_code']
  }))
}

/**
 * 根据出库单安排托盘明细获取可用出库库位
 * @param code
 * @param type
 * @returns {Promise<*>}
 */
export async function getLocationsByHouse(code, type) {
  const {rows: result} = await fetch.get('/warehouse/definition/deviceStatus/index_grid_data', {
    params: {
      house_code: code,
      device_type: type
    }
  })
  return result.map(item => ({
    label: item['device_name'],
    value: item['device_code']
  }))
}

export async function getMaterialInLocations() {
  const {object: result} = await fetch.get('/wms/outbounds/materialLocations')
  return Object.keys(result).map(key => ({
    label: result[key],
    value: key
  }))
}
// 订单箱出库
export const requisitionOrderOrderStockOut = (data) => fetch.post('/platform/definition/requisitionOrder/OrderStockOut', data)

export async function getOutLocations() {
  // const { object: result } = await fetch.get('/wms/outbounds/stayWireLocations', {
  //   params: {
  //     codeStr: outPermissionCodes.join()
  //   }
  // })
  // try {
  //   return Object.keys(result).map(key => {
  //     const val = JSON.parse(result[key])
  //     const extra = {}
  //     if (val.locations) {
  //       extra.children = Object.keys(val.locations).map(key => {
  //         return {
  //           label: val.locations[key],
  //           value: key
  //         }
  //       })
  //     }
  //
  //     return {
  //       label: val.label,
  //       value: key,
  //       ...extra
  //     }
  //   })
  // } catch (e) {
  //   console.log(e)
  //   return []
  // }
}

/**
 * 获取所有的组盘口跟领料口
 * @returns {Promise<void>}
 */
export async function getAllLocations() {
  // const locations = []
  // const locationGroup = await getOutLocations()
  // locations.push({
  //   label: '出库口',
  //   value: 'output',
  //   children: locationGroup
  // })
  //
  // const entries = await getEntrance()
  //
  // locations.push({
  //   label: '入库口',
  //   value: 'input',
  //   children: entries.map(item => ({
  //     label: item.value,
  //     value: item.key
  //   }))
  // })
  // return locations
}

export const outboundPagedUrl = '/platform/definition/requisitionOrder'

export const addOutbound = (data) => fetch.post('/platform/definition/requisitionOrder/add', data)

export const addScrap = (data) => fetch.post('/platform/definition/requisitionOrder/scrap', data)

export const getOutboundMaterials = (id) => '/platform/definition/requisitionOrder/items'

export const queryOutboundMaterials = (params) => fetch.get('/platform/definition/requisitionOrder/items', { params })

// 安排
export const allocateOutbound = (data) => fetch.post(`/platform/definition/requisitionOrder/allotOrder`, data)

// 取消分配
export const cancelAllot = (data) => fetch.post(`/platform/definition/requisitionOrder/cancelAllot`, data)

// 标记完成
export const finishOutbound = (id) => fetch.put(`/platform/definition/requisitionOrder/finishOrder/${id}`)

// 审核过账
export const submitOutbound = (data) => fetch.put(`/platform/definition/requisitionOrder/submitOrder`, data)

// 作废
export const cancelOutbound = (data) => fetch.post(`/platform/definition/requisitionOrder/cancel`, data)

// 生成下架单
export const generateOutbound = (form_id) => fetch.put(`/platform/definition/requisitionOrder/generateOutbound/${form_id}`)

// 下架单
export const offshelfPagedUrl = `/platform/definition/outbound`
export const offshelfDetailPagedUrl = (formNo) => `/platform/definition/outboundItem/${formNo}`

export const getDeviceExit = (data) => fetch.get('/warehouse/definition/deviceStatus/device/exit', {
  params: data
})

export const manualAllocationPagedUrl = '/platform/definition/requisitionOrder/manualAllocation'

export const submitAllocation = (data) => fetch.post('/platform/definition/requisitionOrder/manualAllocationSave', data)

export const findAvailableMaterial = `/warehouse/inventory/storageMaterial/availableMaterial`

/**
 * 创建波次单
 * @param data
 * @returns {*}
 */
export const createWave = (data) => fetch.post(`/platform/definition/requisitionOrder/doWave`, data)

export const createPickOrder = (data) => fetch.post(`/platform/definition/requisitionOrder/picking`, data)