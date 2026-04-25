import SvgIcon from '@/components/SvgIcon'
import Vue from 'vue'

Vue.component('SvgIcon', SvgIcon)

const requireContext = require.context('./svg', false, /\.svg$/)
requireContext.keys().map(requireContext)
