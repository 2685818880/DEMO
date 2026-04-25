import fetch from '@/utils/fetch'
import qs from 'qs'

/**
 * 获取仓库信息
 */
function getStoreHouse() {
  return fetch.get('/warehouse/definition/warehouse/store')
}

/**
 * 获取库位信息
 * @returns {*}
 */
function getLocations(params) {
  return fetch.get('/warehouse/definition/storageLocation/locations', {
    params
  })
}

/**
 * 锁操作
 * @param dto
 * @returns {*}
 */
function updateLock(dto) {
  return fetch.post('/warehouse/definition/storageLocation/lock', dto)
}

/**
 * 获取锁信息
 */
export const getLockInfo = '/warehouse/definition/storageLocation/locks/search'

/**
 * 获取锁原因
 */
function getLockRemark(id, dirt) {
  return fetch.get(`/warehouse/definition/storageLocation/lock/${id}/remark`, {
    params: {
      direct: dirt
    }
  })
}

function dellockInfo(id, loc, houseNo, direct) {
  return fetch.delete(`/warehouse/definition/storageLocation/lock/del/${id}`, {
    params: {
      loc, houseNo, direct
    }
  })
}

/**
 * 获取电芯信息
 * @returns {*}
 * @param dispatchId
 */
function getMaterials(containerCode) {
  return fetch.get(`/warehouse/inventory/containerMaterial/${containerCode}/materials`)
}

/**
 * 获取库存信息
 * @param houseNo
 * @param loc
 * @returns {*}
 */
function getLocationStorage(loc, houseNo) {
  return fetch.get(`/warehouse/inventory/storageLocationInventory/${loc}/loc`, {
    params: {
      houseCode: houseNo
    }
  })
}

export function clearError(id) {
  return fetch.get(`/warehouse/definition/storageLocation/setNormal`, {
    params: {
      id
    }
  })
}
export function resetLocation(id) {
  return fetch.get(`/warehouse/definition/storageLocation/resetLoc`, {
    params: {
      id
    }
  })
}

export function getInventoryStatistic() {
  return fetch.get('/wms/custom/inventory/statistic')
}

// 人工搬运
export const storageCarry = (data) => fetch.post('/warehouse/inventory/storageLocationInventory/index_carry_submit', qs.stringify(data))

export {
  getStoreHouse,
  getLocations,
  updateLock,
  // getLocationReport,
  getLockRemark,
  dellockInfo,
  getMaterials,
  getLocationStorage
}
