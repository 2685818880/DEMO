import fetch from '@/utils/fetch'

export const addOrder = (model) => fetch.post('/platform/definition/requisitionOrder/addOrder', model)

export const addOrderItem = (model) => fetch.post('/platform/definition/requisitionOrder/addOrderItem', model)
