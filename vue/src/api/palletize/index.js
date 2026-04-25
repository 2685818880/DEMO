import fetch from '@/utils/fetch'
import {enterPermissionCodes} from '@/utils/auth'

/**
 * 分页查询
 */
export const palletizePagedUrl = '/platform/definition/palletize/page'
/**
 * 组盘明细分页查询
 */
export const palletizeItemPagedUrl = (pFormNo) => `/platform/definition/palletizeItem/${pFormNo}`
/**
 * 获取物料信息
 */
export const queryMaterial = (dto) => fetch.post('/platform/definition/palletize/query', dto)

/**
 * 呼叫空托
 */
export const callContainer = (dto) => fetch.post('/platform/definition/palletize/call_container', dto)
/**
 * 组盘单作废
 */
export const unPalletizeContainer = (dto) => fetch.post('/platform/definition/palletize/cancel', dto)
/**
 * 获取托盘类型
 */
export const getContainerType = () => fetch.get('/warehouse/container/containerType/list', {
  params: {
    codeStr: enterPermissionCodes.join()
  }
})

/**
 * 注册空托
 */
export const registerContainer = (dto) => fetch.post('/platform/definition/palletize/register', dto)
/**
 * 容器校验
 * @param dto
 */
export const checkContainer = (dto) => fetch.get('/warehouse/container/container/check', {
  params: dto
})

export const getEmptyCount = (house_code, to_pos) => fetch.get('/wms/custom/empty/amount', {
  params: {
    house_code,
    to_pos
  }
})

// 获取默认组盘类型
export const getPalletizeType = () => fetch.get('/platform/definition/palletize/isSnPalletize')

export const relieveMaterials = (data) => fetch.post('/platform/definition/palletize/relieve', data)

// 根据托盘和库号获取物料
export const getPalletizeItem = (params) => fetch.get('/platform/definition/palletize/query/item', { params })

export const startPalletize = (dto) => fetch.post('/platform/definition/palletize/palletize', dto)

export const checkAsn = (params) => fetch.get('/platform/definition/asn/checkasncodecont', {params})

export const queryTypeFlag = () => fetch.get('/platform/definition/palletize/type_flag')

const PalletizePrefix = '/wms/palletize'
export const queryDeviceCode = () => fetch.get(`${PalletizePrefix}/getDeviceCode`)

export const queryMes = (params) => fetch.get(`${PalletizePrefix}/queryMes`, { params })

export const savePalletize = (data) => fetch.post(`${PalletizePrefix}/save`, data)

export const relievePalletize = (data) => fetch.post(`${PalletizePrefix}/relieve`, data)
