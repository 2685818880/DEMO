import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)
export default new Router({
  // 这个为后端的模块名 + view（约定占位）
  /* eslint-disable */
  base: process.env.BASE_URL,
  mode: 'history',
  routes: [
    {
      path: '/demo',
      name: 'demo',
      component: () => import('@/views/demo')
    },
    {
      path: '/outbound/:formType?/:remark?',
      name: 'Outbound',
      component: () => import('@/views/outbound/material')
    },
    {
      path: '/report-material',
      name: 'MaterialReport',
      component: () => import('@/views/report/material')
    },
    {
      path: '/material-sum',
      name: 'MaterialSum',
      component: () => import('@/views/report/sum')
    },
    {
      path: '/manual/tray/bind',
      name: 'TrayBind',
      component: () => import('@/views/manual/tray/bind')
    },
    {
      path: '/manual/tray/relieve',
      name: 'TrayUnbind',
      component: () => import('@/views/manual/tray/relieve')
    },
    {
      path: '/workstation',
      name: 'Workstation',
      component: () => import('@/views/workstation')
    },
    // 表单设计器
    {
      path: '/form-designer/list',
      name: 'FormDesignerList',
      component: () => import('@/form-designer/views/FormDesignerList'),
      meta: { title: '表单设计器', icon: 'form' }
    },
    {
      path: '/form-designer/design/:id?',
      name: 'FormDesignerDesign',
      component: () => import('@/form-designer/views/FormDesignerDesign'),
      meta: { title: '表单设计', icon: 'edit' }
    },
    {
      path: '/form-designer/database',
      name: 'DatabaseConfig',
      component: () => import('@/form-designer/views/DatabaseConfig'),
      meta: { title: '数据库配置', icon: 'database' }
    },
    {
      path: '/ai/chat-bi',
      name: 'ChatBI',
      component: () => import('@/views/ai/ChatBI')
    },
    {
      path: '/ai-external',
      redirect: '/ai/providers'
    },
    {
      path: '/ai/providers',
      name: 'AiProviders',
      component: () => import('@/views/ai/ProviderManagement')
    }
  ]
})
