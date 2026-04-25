import Vue from 'vue'
import App from './App'
import router from './router'
import i18n from './i18n'
import '@/icons'
import Element from 'element-ui'
import '@/styles/index.scss'
import '@/filters'
import '@/components/ContextMenu'
import TiTan from '@wxzd/titan-ui'
import '@wxzd/titan-ui/dist/titan-ui.css'
import enumOptions from '@utils/enums-options'
import '@/directives'
// import LocationTree from '@comp/LocationTree/index'
// import StorageMap from '@comp/storage-map'
import BizSelect from '@comp/BizSelect'

const baseURL = process.env.API_BASE_URL

Vue.use(Element, {
  size: 'small',
  i18n: (key, value) => i18n.t(key, value)
})

Vue.use(TiTan, {
  enums: enumOptions,
  baseURL,
  fields: {
    components: {
      'locationTree': {
        compName: 'LocationTree'
      },
      'bizSelect': {
        compName: 'BizSelect'
      }
    }
  }
})

// Vue.component(LocationTree.name, LocationTree)
// Vue.component('StorageMap', StorageMap)
Vue.component(BizSelect.name, BizSelect)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  i18n,
  render: h => h(App),
  created() {
    Vue.prototype.i18n = Vue.prototype.$t
  }
}).$mount('#app')
