import fetch from '@/utils/fetch'

export const download = (params) => {
  try {
    const { id, fileName } = params
    window.location.href = `${process.env.API_BASE_URL}/common/download?fileName=${fileName}&delete=${params.DELETE}&id=${id}`
    return Promise.resolve()
  } catch (err) {
    return Promise.reject(err)
  }
}

export const load = (params, heads, url) => {
  return fetch.post(url, {
    ...params,
    heads
  })
}
