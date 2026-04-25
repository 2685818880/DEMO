import fetch from '@/utils/fetch'

/**
 * 获取库存可用汇总信息
 * @type {string}
 */
export const queryAvailableStorage = '/warehouse/inventory/storageMaterial/available'

// export const queryInventoryStorageMaterials = '/warehouse/inventory/storageMaterial/index_sum_container_code_data'
export const queryInventoryStorageMaterials = '/wms/custom/index_grid_data_v'

export const queryStorageAllocate = `/warehouse/inventory/storageMaterial/index_storage_data`

export const queryQualityStorageMaterials = `/warehouse/inventory/storageMaterial/index_quality_sum_data`

export const queryInventoryAllocate = `/warehouse/inventory/storageMaterial/index_sum_inventory_data`

/**
 * 查询库区信息
 */
export const queryAllHouse = () => fetch.get('/warehouse/definition/warehouse/houseNos')

/**
 * 获取所有库区信息
 */
export const getAllHouse = () => fetch.get('/warehouse/definition/warehouse/house')

// 根据托盘和库号获取物料
export const getContainerMaterials = (params) => fetch.get('/warehouse/inventory/containerMaterial/index_list_data', { params })

export const relieveMaterials = (data) => fetch.post('/platform/definition/palletize/relieve', data)

// 查询sku
export const skuPagedUrl = `/warehouse/material/sku/index_grid_data?xxx=1`

// 查询客户
export const getCustomer = (params) => fetch.get('/warehouse/company/customer/index_query_paged', { params })

// 查询供应商
export const getSupplier = (params) => fetch.get('/warehouse/company/supplier/index_query_paged', { params })

// 查询sku汇总
export const skuSummaryUrl = `/warehouse/inventory/skuSummary/index_grid_data`

export const queryDevice = (params) => fetch.get('/warehouse/definition/deviceStatus/index_grid_data', { params })
