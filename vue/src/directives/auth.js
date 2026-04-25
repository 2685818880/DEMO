import Vue from 'vue'
import { permissions } from '@/utils/auth'

/**
 * 权限校验
 * 用法 v-has="demo:add" 当permissions时会自动删除该元素
 */
Vue.directive('has', {
  inserted(el, { value }) {
    if (!permissions.includes(value)) {
      el.parentNode.removeChild(el)
    }
  }
})
