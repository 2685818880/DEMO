import fetch from '@/utils/fetch'

export const predictInventory = (data) => fetch.post('/restful/api/ai/inventory/predict', data)

export const optimizePath = (data) => fetch.post('/restful/api/ai/path/optimize', data)

export const detectAnomalies = (data) => fetch.post('/restful/api/ai/anomaly/detect', data)

export const getAgentStatus = () => fetch.get('/restful/api/ai/status')

export const healthCheck = () => fetch.get('/restful/api/ai/health')

export const chatBiQuery = (data) => fetch.post('/restful/api/ai/chat-bi/query', data)

export const createTransferFromChatBI = (data) => fetch.post('/restful/api/ai/chat-bi/create-transfer', data)

export const createStocktakeFromChatBI = (data) => fetch.post('/restful/api/ai/chat-bi/create-stocktake', data)

export const createAsnFromChatBI = (data) => fetch.post('/restful/api/ai/chat-bi/create-asn', data)

export const createRequisitionFromChatBI = (data) => fetch.post('/restful/api/ai/chat-bi/create-requisition', data)

export const createPalletizeFromChatBI = (data) => fetch.post('/restful/api/ai/chat-bi/create-palletize', data)

export const generateSchedule = (data) => fetch.post('/restful/api/ai/scheduling/generate', data)

export const executeAllocation = (data) => fetch.post('/restful/api/ai/allocation/execute', data)

export const getWarehouses = () => fetch.get('/restful/api/ai/warehouses', { params: { status: 'ACTIVE' } })

export const getLocations = (houseCode) => fetch.get('/restful/api/ai/locations', { params: { houseCode } })

// Provider Management
export const listProviders = (params) => fetch.get('/restful/api/ai/provider/list', { params })

export const getProvider = (id) => fetch.get('/restful/api/ai/provider/' + id)

export const createProvider = (data) => fetch.post('/restful/api/ai/provider/create', data)

export const updateProvider = (id, data) => fetch.put('/restful/api/ai/provider/' + id, data)

export const deleteProvider = (id) => fetch.delete('/restful/api/ai/provider/' + id)

export const hostApiKey = (id, apiKey) => fetch.post('/restful/api/ai/provider/' + id + '/api-key', { apiKey })

export const updateApiKey = (id, apiKey) => fetch.put('/restful/api/ai/provider/' + id + '/api-key', { apiKey })

export const deleteApiKey = (id) => fetch.delete('/restful/api/ai/provider/' + id + '/api-key')

export const batchDeleteProviders = (ids) => fetch.post('/restful/api/ai/provider/batch/delete', { ids })

export const batchUpdateApiKey = (ids, apiKey) => fetch.post('/restful/api/ai/provider/batch/update-key', { ids, apiKey })

export const listModels = (providerId) => fetch.get('/restful/api/ai/provider/' + providerId + '/models')

export const toggleModel = (providerId, modelId, enabled) => fetch.put('/restful/api/ai/provider/' + providerId + '/models/' + modelId + '/toggle', { enabled })

export const testModel = (modelId) => fetch.get('/restful/api/ai/provider/models/' + modelId + '/test')

export const checkKeyStatus = () => fetch.get('/restful/api/ai/provider/check-key-status')
