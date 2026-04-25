import fetch from '@/utils/fetch'

export const requisitionItemPagedUrl = '/platform/definition/requisitionItem/queryAll'

export const createWave = (data) => fetch.post('/platform/definition/requisitionItem/createWave', data)

export const comboWave = (data) => fetch.post('/wms/project/wave/add', data)

export const cancelWaveItem = (data) => fetch.post('/wms/project/wave/cancelItem', data)

export const createUrgentWave = (data) => fetch.post('/platform/definition/requisitionItem/createUrgentWave', data)

export const markUrgent = (data) => fetch.post('platform/definition/requisitionItem/markUrgent', data)
