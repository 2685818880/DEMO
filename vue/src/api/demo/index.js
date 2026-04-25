import fetch from '@/utils/fetch'

export const pagedUrl = '/demo/search'

export const save = (dto) => fetch.post('/demo/save', dto)

export const del = (id) => fetch.delete(`/demo/delete/${id}`)
