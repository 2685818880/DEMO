import Vue from 'vue'
import Vuex from 'vuex'
import {getCookie} from '@/utils/cookie'

Vue.use(Vuex)

const locale = getCookie('i18n') || ''

export default new Vuex.Store({
  state: {
    locale: locale.replace('_', '-') || 'zh'
  }
})
