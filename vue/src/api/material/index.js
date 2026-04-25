import fetch from '@/utils/fetch'
import qs from 'qs'

const PREFIX = '/warehouse/material'

export const getSkuCategory = (params = {}) => fetch.get(`${PREFIX}/skuCategory/index_grid_data`, {
  params
})
