<template>
  <el-button
    icon="el-icon-download"
    :loading="loading"
    @click="handleExport">
    {{ i18n('导出') }}
  </el-button>
</template>

<script>
import { loading, confirm } from '@/decorator/index'
import { parseTime } from './utils'
import {
  load, download
} from './api'

export default {
  name: 'ExportButton',

  props: {
    columns: {
      type: Array,
      default: () => []
    },
    url: {
      type: String,
      default: '/warehouse/inventory/storageMaterial/load'
    },
    params: {
      type: Object,
      default: () => ({})
    },
    dateRange: {
      type: Object,
      default: () => ({
        startTime: '',
        endTime: ''
      })
    },
    beforeExport: {
      type: Function,
      default: () => {}
    }
  },

  data() {
    return {
      loading: false
    }
  },

  computed: {
    filterColumns() {
      return this.columns.filter(item => !item.hidden && item.prop)
    }
  },

  methods: {
    @confirm('是否下载文件?')
    @loading()
    async handleExport() {
      await this.beforeExport()
      // loading("正在导出数据，请稍后...")
      const colum = this.filterColumns
      const heads = colum.map(item => {
        let param = {}
        param[item.prop] = item.label
        return param
      })
      const validres = this.validDateRange()
      if (!validres) {
        this.$message({
          message: '请选择一个月以内的日期范围',
          type: 'warning'
        })
        return false
      }
      const { startTime, endTime } = validres
      this.params.start_time = startTime
      this.params.end_time = endTime
      const loadres = await load(this.params, heads, this.url)
      console.log(loadres)
      if (loadres.success) {
        const data = loadres.object
        const params = {
          fileName: data.fileName,
          id: data.id,
          DELETE: true
        }
        await download(params)
      }
    },

    validDateRange() {
      const { startTime, endTime } = this.dateRange
      const res = {
        startTime,
        endTime
      }
      if (!startTime || !endTime) {
        res.startTime = parseTime(new Date().setMonth((new Date().getMonth() - 1)))
        res.endTime = parseTime((new Date()).getTime())
      } else {
        const start = new Date(startTime).getTime()
        const end = new Date(endTime).getTime()
        const limit = end - (31 * 24 * 3600 * 1000)
        if (start < limit) {
          return false
        }
      }
      return res
    }
  }
}
</script>
