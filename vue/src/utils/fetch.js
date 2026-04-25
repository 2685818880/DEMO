import axios from 'axios'
import {Message} from 'element-ui'
// import {Notification, Message} from 'element-ui'
import getErrorMsg from './errorMsg'

// 代理配置
const proxyApi = {
  // '/bm01': '/bm01'
  // '/fts': '/fts/api'
}

axios.defaults.baseURL = process.env.API_BASE_URL

axios.defaults.headers['X-Requested-With'] = 'XMLHttpRequest'

/**
 * request拦截器
 */
axios.interceptors.request.use(config => {
  config.withCredentials = true

  Object.keys(proxyApi).some((item) => {
    if (config.url.startsWith(item)) {
      config.url = config.url.replace(item, proxyApi[item])
    }
  })

  return config
}, err => {
  Promise.reject(err)
})

/**
 * response 拦截器
 */
axios.interceptors.response.use(res => {
  const data = res.data
  // 异常处理
  if (res.config.url.indexOf('restful/i18nRestful/language') === -1 && (
    typeof data === 'string' || (typeof data.success === 'boolean' && !data.success) || data.code === 500
  )) {
    // Notification({
    //   message: data.message || '处理异常',
    //   type: 'error',
    //   duration: 5 * 1000
    // })
    Message({
      message: data.message || '处理异常',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(new Error(data.message))
  }

  if (res.config.method !== 'get') {
    // Notification({
    //   // title: '成功',
    //   title: '操作成功',
    //   type: 'success',
    //   duration: 5 * 1000
    // })
    Message({
      message: '操作成功',
      type: 'success',
      duration: 5 * 1000
    })
  }

  return res.data
}, error => {
  const message = error.response.data.message || getErrorMsg(error.response.status)

  // Notification({
  //   message: message,
  //   type: 'error',
  //   duration: 5 * 1000
  // })
  Message({
    message: message,
    type: 'error',
    duration: 5 * 1000
  })

  return Promise.reject(new Error(message))
})

export default axios
