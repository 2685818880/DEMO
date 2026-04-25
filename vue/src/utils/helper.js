import * as Enums from './enums'
import * as EnumsColor from './enum-color'

/**
 * 获取枚举
 * @param code
 * @returns {{}}
 */
export const getEnum = (code) => {
  const enumMap = Enums[code]
  if (!enumMap) {
    console.warn(`${code}对应的枚举不存在`)
    return {}
  }

  return enumMap
}

/**
 * 将枚举转换成数组
 * @param code
 * @returns {{label: *, value: string}[]}
 */
export const generateEnumOptions = (code) => {
  const enumMap = getEnum(code)
  return Object.keys(enumMap).map(key => ({
    value: key,
    label: enumMap[key]
  }))
}

/**
 * 获取枚举label
 * @param typeCode 枚举类，详见enum.js
 * @param key 枚举key
 * @param errLabel 异常信息
 * @returns {*|string}
 */
export const getEnumLabel = (typeCode, key, defaultLabel = '-', defaultColor = '#333') => {
  const enumMap = getEnum(typeCode)
  const enumColorMap = getEnumColor(typeCode)
  const enumLabel = enumMap[key] || key || defaultLabel
  const enumColor = enumColorMap[key] || defaultColor
  return `<span style="color:${enumColor}">${enumLabel}</span>`
}

export const getEnumColor = (code) => {
  const enumMap = EnumsColor[code]
  if (!enumMap) {
    console.warn(`${code}对应的枚举不存在`)
    return {}
  }

  return enumMap
}

/**
 * 时间提醒
 * @returns {string}
 */
export function timeFix() {
  const time = new Date()
  const hour = time.getHours()
  return hour < 9 ? '早上好' : (hour <= 11 ? '上午好' : (hour <= 13 ? '中午好' : (hour < 20 ? '下午好' : '晚上好')))
}

/**
 * 简单的深拷贝
 * @param obj
 * @returns {any}
 */
export function deepCopy(obj) {
  return JSON.parse(JSON.stringify(obj))
}
