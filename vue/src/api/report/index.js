import fetch from '@/utils/fetch'

export const materialReportPagedUrl = '/report/material/list'

export const queryMaterialSum = () => fetch.get('/report/material/sum')
