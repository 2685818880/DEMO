import fetch from '@/utils/fetch'

export const queryDeviceCode = () => fetch.get(`/wms/palletize/getDeviceCode`)
