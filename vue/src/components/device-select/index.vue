<template>
  <el-cascader
    v-bind="$attrs"
    :options="options"
    v-on="$listeners"
  />
</template>

<script>
import { queryDeviceCode } from './api'

export default {
  name: 'DeviceSelect',

  data() {
    return {
      options: []
    }
  },

  created() {
    this.getDeviceCode()
  },

  methods: {
    async getDeviceCode() {
      try {
        const locationTypeMap = {
          A: '阳极',
          B: '阴极'
        }
        const { object } = await queryDeviceCode()
        const res = []
        Object.keys(object).map(key => {
          res.push({
            label: locationTypeMap[key],
            value: key,
            children: object[key].split(',').map(item => ({
              label: item,
              value: item
            }))
          })
        })
        this.options = res
      } catch (error) {
        console.warn(error, '获取设备失败')
      }
    }
  }
}
</script>
