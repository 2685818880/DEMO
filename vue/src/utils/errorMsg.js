export default function getErrorMsg(code) {
  switch (code) {
    case 400:
      return '请求参数异常'
    case 401:
      return '密码错误或账号不存在'
    case 403:
      return '无访问权限'
    default:
      return '系统错误'
  }
}
