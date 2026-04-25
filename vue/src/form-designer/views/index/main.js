import Vue from 'vue'
import App from './App.vue'
import router from '@/form-designer/router'
import '@/form-designer/styles/index.scss'
import '@/icons'
import axios from 'axios'
import Tinymce from '@/form-designer/components/tinymce/index.vue'

Vue.component('tinymce', Tinymce)

Vue.config.productionTip = false
Vue.prototype.$axios = axios

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
