/* cookie操作工具库 */

/**
 * 获取cookie
 * @param name: 需要查询的cookie名称
 * @returns {string|''}
 */
export function getCookie(name) {
  const reg = new RegExp(`(^| )${name}=([^;]*)(;|$)`)
  const cookie = document.cookie
  let res = cookie.match(reg)
  if (res) {
    return decodeURIComponent(res[2])
  }
  return ''
}
