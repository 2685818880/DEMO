export function dateFtt(fmt, date) {
  let o = {
    'M+': date.getMonth() + 1, // 月份
    'd+': date.getDate(), // 日
    'h+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    'S': date.getMilliseconds() // 毫秒
  }
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  for (let k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)))
    }
  }
  return fmt
}

export function formatDatetime(value = '', dateFormat = 'yyyy-MM-dd hh:mm:ss') {
  let format = ''

  typeof value === 'number' &&
  (value = value.toString().length === 10 ? value * 1000 : value)

  if (value) {
    format = dateFtt(dateFormat, new Date(value))
    return format
  }
  return format
}

export function formatMoney(value = '') {
  if (!value) return ''
  value = value.toString()
  if (value && value.includes(',')) return value

  // 获取整数部分
  let intPart = Number(value) - Number(value) % 1

  // 将整数部分逢三一断
  let intPartFormat = intPart.toString().replace(/(\d)(?=(?:\d{3})+$)/g, '$1,')

  // 预定义小数部分
  let floatPart = '.00'
  let value2Array = value.toString().split('.')

  // =2表示数据有小数位
  if (value2Array.length === 2) {
    floatPart = value2Array[1].toString() // 拿到小数部分
    // 补零
    if (floatPart.length === 1) {
      return intPartFormat + '.' + floatPart + '0'
    } else {
      return intPartFormat + '.' + floatPart
    }
  } else {
    return intPartFormat + floatPart
  }
}

export function formatTag(value) {
  (typeof value === 'string') && (value = value.split(','))
  return value
}

export function formatPercent(num, total) {
  num = parseFloat(num)
  total = parseFloat(total)
  if (isNaN(num) || isNaN(total)) {
    return '-'
  }
  return total <= 0 ? 0 : Math.round((num / total) * 10000) / 100.0 + '%'
}

export function defaultFormat(value) {
  return value
}

const formats = {
  formatDatetime,
  formatMoney,
  formatTag,
  formatPercent,
  defaultFormat
}

export default formats
