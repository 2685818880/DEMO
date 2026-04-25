import Vue from 'vue'
import { getEnumLabel } from '@utils/helper'

/**
 * 枚举过滤器
 * 使用 {{ status | dictText(EnumCode) }}
 */
Vue.filter('dictText', (value, code) => {
  return getEnumLabel(code, value)
})
