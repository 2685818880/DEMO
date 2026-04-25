import fetch from '@/utils/fetch'

// 查询
export const workstationPagedUrl = `/restful/api/workstation/page`

// 新增
export const addWorkstation = (model) => fetch.post('/restful/api/workstation/insert', model)

// 删除
export const delWorkstation = (model) => fetch.post('/restful/api/workstation/delete', model)

// 修改
export const updateWorkstation = (model) => fetch.post('/restful/api/workstation/update-status', model)
